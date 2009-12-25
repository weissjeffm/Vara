(ns vara
  (:use  com.ashafa.clutch compojure)
  (:import (bcrypt BCrypt)))
(def mydb "vara")

(def datatypes #{:fieldTypeDef :field :user :recordTypeDef :record})

(defn valid-datatype? [kw]
  (contains? datatypes kw))

(defn db-id "Puts together a couchdb document id from a datatype keyword and a string"
  [kw str]
  (if-not (valid-datatype? kw)
    (throw (IllegalArgmentException. (format "Invalid db datatype %s, known types are %s" (str kw) (str datatypes))))
    (keyword (str (name kw) "-" str))))

(defn create [docmap id]
  (with-db mydb (create-document docmap id)))

(defn create-field-typedef [name fn-form]
  (create {:name name 
	   :type "fieldTypeDef"
	   :validator_fn (str fn-form)},
	  (db-id :fieldTypeDef name)))

(defn create-field [name type fn-form]
  (create {:name name 
	   :type "field"
	   :typedef_id (str "fieldTypeDef-" type),
	   :validator_fn (str fn-form)} (db-id :field name)))

(defn validate-field " Validator functions should always take
these two arguments, and return a reason why the value is invalid, or nil if it's valid."
  [fn-str field value]
  (if fn-str
    (let [validation-error (try ((eval (read-string fn-str)) field value) 
		      (catch Exception e 
			(throw (IllegalArgumentException. (format "Invalid validator function: %s" fn-str) e))))]
      (if validation-error 
	(throw (Exception. (format "The field '%s' %s. Value given was %s" 
				   (:name field)
				   validation-error 
				   (str value))))))))

(defn validate-fields [typedef fieldmap]
  (doseq [field-id (:field_ids typedef)]
    (let [field (with-db mydb (get-document field-id))
	  name (:name field)
	  type (with-db mydb (get-document (:typedef_id field)))
	  value (fieldmap (keyword name))]
      (validate-field (:validator_fn type) field value)
      (validate-field (:validator_fn field) field value))))

(defn create-user [userid realname email password]
  (let [hash (bcrypt.BCrypt/hashpw password (bcrypt.BCrypt/gensalt))]
    (create {:type "user"
	     :userid userid
	     :email email
	     :password hash
	     :realname realname},
	    (db-id :user userid))))

(defn create-record [type fieldmap]
  (let [typestr (name type)
	typedef-id (db-id :recordTypeDef typestr)
	typedef (with-db mydb 
		  (get-document typedef-id))]
    (validate-fields typedef fieldmap)
    
    (with-db mydb (create-document (assoc fieldmap 
				     :type typestr
				     :typedef_id typedef-id)))))

(defroutes my-app
  (GET "/"
    (html [:h1 "Hello World"]))
  (ANY "*"
    (page-not-found)))


(defn start-compojure []
  (run-server {:port 8080}
	     "/*" 
	     (servlet my-app) ))

(defn login-controller [session params]
  (dosync
    (if
      (and
        (= "secret" (params :password))
        ; Username can include letters, numbers,
        ; spaces, underscores, and hyphens.
        (.matches (params :name) "[\\w\\s\\-]+"))
      (do
        (alter session assoc :name (params :name))
        (redirect-to "/articles/"))
      (redirect-to "/login/"))))

(defn login-controller2 [session params]
  (dosync 
   (if 
       )))