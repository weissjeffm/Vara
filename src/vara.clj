(ns vara
  (:use  com.ashafa.clutch 
	 compojure)
  (:import (bcrypt BCrypt)))
(def mydb "vara")

(def datatypes #{:fieldTypeDef :field :user :group :recordTypeDef :record})

(defn valid-datatype? [kw]
  (contains? datatypes kw))

(defn db-id "Puts together a couchdb document id from a datatype keyword and a string"
  [kw id-str]
  (if-not (valid-datatype? kw)
    (throw (IllegalArgumentException. (format "Invalid db datatype %s, known types are %s" (str kw) (str datatypes))))
    (str (name kw) "-" id-str)))

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
(defn create-group [groupid description userid-list]
     (create {:type "group"
	      :groupid groupid
	      :description description
	      :members userid-list}
	     (db-id :group groupid)))

(defn is-member? [id idseq]
  (let [group? (fn [thisid] (.startsWith thisid "group-"))]
    (if (contains? (set idseq) id) true
	(if (some group? idseq) 
	  (recur id (let [groups (filter group? idseq)
			  first-exp (:members (with-db mydb (get-document (first groups))))
			  rest (rest groups)]
		      (into first-exp rest)))
	  false))))

(comment (defn login-controller [session params]
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
      (redirect-to "/login/")))))

(defn login-view [msg]
  [ (html
     [:form {:method "post"}
      "User name: "
      [:input {:name "userid-attempt", :type "text"}]
      [:br]
      "Password: "
      [:input {:name "password-attempt", :type "password"}]
      [:br]
      [:input {:type "submit" :value "Log in"}]]
     (if msg [:h2 msg]))])

(defn login-controller [params session]
  (dosync 
   (if (params :password-attempt)
     (do			      ;get the user from the db if any
       (let [userid (params :userid-attempt)
	     user (with-db mydb
		    (get-document (db-id :user userid)))]
	 (if user 
	   (let [dbhash (:password user)]
	     (if (bcrypt.BCrypt/checkpw (params :password-attempt) dbhash)
	       (conj (redirect-to "/hello") {:session (session-assoc :loggedin userid)})
	       (login-view "Invalid credentials, please try again.")))
	   (login-view "Unknown user, please try again."))))
     (login-view "woopsies something went really wrong here."))))

(declare testjeff)

(defroutes my-app
  (GET "/login/" (login-view flash))
  (POST "/login/" (login-controller params session))
  (GET "/test/" (let [stuff (testjeff)]
		  (println session)
		  (assoc stuff :session (into session (:session stuff)))))
  ;(ANY "/logout/" (logout-controller session))   
  (GET "/hello"
	(html [:h1 "Hello World"]))
   (ANY "*"
	(page-not-found)))

(decorate my-app (with-session :memory))

(defn start-compojure []
  (run-server {:port 8080}
	     "/*" 
	     (servlet my-app)))

(defn testjeff []
  {:body (html [:h1 "yabbo"]) :session {:myvar "myvalue"}})