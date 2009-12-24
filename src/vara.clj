(ns vara
  (:use com.ashafa.clutch))
(def mydb "vara")

(defn create-field-typedef [name fn-form]
  (with-db mydb (create-document {:name name 
				  :type "fieldTypeDef"
				  :validator_fn (str fn-form)} (str "fieldTypeDef-" name))))

(defn validate "takes a fieldname, a document, and a value.  Will extract the validator fn from the document,
and call the function with the document and the value as arguments. Validator functions should always take
these two arguments, and return a reason why the value is invalid, or nil if it's valid."
  [field-name self value]
  (let [validator (:validator_fn self)
	result (try ((eval (read-string validator)) self value) 
		    (catch Exception e 
		      (throw (IllegalArgumentException. (format "Invalid validator function: %s" validator) e))))]
    (if (not (nil? result)) 
      (throw (Exception. (format "The field '%s' %s. Value given was %s" field-name result (str value)))))))

(defn validate-fields [typedef fieldmap ]
  (doseq [field-id (:field_ids typedef)]
    (let [field (with-db mydb (get-document field-id))
	  name (:name field)
	  type (with-db mydb (get-document (:typedef_id field)))
	  value (fieldmap (keyword name))]
      (validate name type value)
      (validate name field value))))

(defn create [type fieldmap]
  (comment (if (not (and (keyword? type) (map? fieldmap))) (throw (Exception. "Expecting a type keyword and "))))
  (let [typestr (name type)
	typedef-id (str "recordTypeDef-" typestr)
	typedef (with-db mydb 
		  (get-document typedef-id))]
    (validate-fields typedef fieldmap)
    
    (with-db mydb (create-document (assoc fieldmap 
				     :type typestr
				     :typedef_id typedef-id)))))