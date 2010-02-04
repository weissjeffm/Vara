(ns vara.core
  (:use
   [vara.api.hooks :only (validate-field)]
   vara.db.utils
   com.ashafa.clutch 
   compojure
   clojure.contrib.def)
  (:import (jBCrypt BCrypt)))


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


(defn validate-fields [typedef fieldmap]
  (doseq [field-id (:field_ids typedef)]
    (let [field (with-db mydb (get-document field-id))
	  name (:name field)
	  type (with-db mydb (get-document (:typedef_id field)))
	  value (fieldmap (keyword name))]
      (validate-field (:validator_fn type) field value)
      (validate-field (:validator_fn field) field value))))

(defn create-user [userid realname email password]
  (let [hash (jBCrypt.BCrypt/hashpw password (jBCrypt.BCrypt/gensalt))]
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
(defn update-record [id fieldmap]
  (let [existing-record (with-db mydb (get-document id))]
    (if-not existing-record 
      (throw (IllegalArgumentException. 
	      (format "Unable to update, record with id %s was not found in the database." id))))
    (let [typedef-id (db-id :recordTypeDef (existing-record :type))
	  typedef (with-db mydb (get-document typedef-id))]
      (validate-fields typedef (into existing-record fieldmap))
      (with-db mydb (update-document existing-record fieldmap) ))))

(defn create-group [groupid description userid-list]
     (create {:type "group"
	      :groupid groupid
	      :description description
	      :members userid-list}
	     (db-id :group groupid)))



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
     (if msg [:h2.errorBlock msg]))])

(defn view-record [record]
  [(html 
    [:div.show-record
     [:table
      [:tbody
       (map (fn [key] [:tr [:td (name key)] [:td (key record)]])
	    (keys record))]]])])

(defn new-record []
  [(html [:div.new-record [:form [:select () ]]])])
(defn hello-view [user] 
  (html [:div.welcome 
	 [:h1 (str "Hello " user)]
	 [:a {:href "view/"}]]))

(defn login-controller [params session]
  (dosync 
   (if (params :password-attempt)
     (do			      ;get the user from the db if any
       (let [userid (params :userid-attempt)
	     user (with-db mydb
		    (get-document (db-id :user userid)))]
	 (if user 
	   (let [dbhash (:password user)]
	     (if (jBCrypt.BCrypt/checkpw (params :password-attempt) dbhash)
	       [(hello-view userid) (session-assoc :loggedin userid)]
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
  (GET "/session"
	(html [:h1 (str "Hello, your session is: " session)]))
   (ANY "*"
	(page-not-found)))

(decorate my-app (with-session :memory))

(defn start-compojure []
  (run-server {:port 8080}
	     "/*" 
	     (servlet my-app)))

(defn testjeff []
  {:body (html [:h1 "yabbo"]) :session {:myvar "myvalue"}})
