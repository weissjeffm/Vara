(ns vara
  (:use com.ashafa.clutch))
(def mydb "vara")

(defn create-field-typedef [name fn-form]
  (with-db mydb (create-document {:name name 
				  :type "fieldTypeDef"
				  :validator_fn (str fn-form)} (str "fieldTypeDef-" name))))
(defn create-field [name type fn-form]
  (with-db mydb (create-document {:name name 
				  :type "field"
				  :typedef_id (str "fieldTypeDef-" type)
				  :validator_fn (str fn-form)} (str "field-" name))))

(defn validate " Validator functions should always take
these two arguments, and return a reason why the value is invalid, or nil if it's valid."
  [fn-str field value]
  (if-not (nil? fn-str)
      (let [result (try ((eval (read-string fn-str)) field value) 
			(catch Exception e 
			  (throw (IllegalArgumentException. (format "Invalid validator function: %s" fn-str) e))))]
	(if (not (nil? result)) 
	  (throw (Exception. (format "The field '%s' %s. Value given was %s" (:name field) result (str value))))))))

(defn validate-fields [typedef fieldmap ]
  (doseq [field-id (:field_ids typedef)]
    (let [field (with-db mydb (get-document field-id))
	  name (:name field)
	  type (with-db mydb (get-document (:typedef_id field)))
	  value (fieldmap (keyword name))]
      (validate (:validator_fn type) field value)
      (validate (:validator_fn field) field value))))

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