(ns vara 
	(:require [clojure.contrib.sql :as sql]
						clj-record.core
						clj-record.associations
						clj-record.validation
						))


 

 
(def mydb {:classname "org.postgresql.Driver" ; must be in classpath
         :subprotocol "postgresql"
         :subname (str "//localhost:5432/vara")
         ; Any additional keys are passed to the driver
         ; as driver-specific properties.
         :user "weissj"
         :password "dog8code"})
         
(def db mydb)

(defmulti get-id-key-spec :subprotocol)
(defmethod get-id-key-spec "derby" [db-spec name]
  [:id :int (str "GENERATED ALWAYS AS IDENTITY CONSTRAINT " name " PRIMARY KEY")])
(defmethod get-id-key-spec :default [db-spec name]
  [:id "SERIAL UNIQUE PRIMARY KEY"])
          
 ;(defn create-bugs
 ; "Create a table to store bug entries"
 ; []
 ; (clojure.contrib.sql/create-table
 ;  :bugs
 ;  (get-id-key-spec mydb)
 ;  [:headline :text]
 ;  [:description :text]))

(defn create-workflows
  "Create a table to store workflows"
  []
  (sql/create-table
   :workflows
   (get-id-key-spec mydb "workflow_pk")
   [:name :text]
   [:description :text]))
   
(defn create-actions
  "Create a table to store actions"
  []
  (sql/create-table
   :actions
   (get-id-key-spec mydb "action_pk")
   [:name :text]
   [:description :text]
   [:workflow_id :int "NOT NULL"]))     

(defn create-states
  "Create a table to store states"
  []
  (sql/create-table
   :states
   (get-id-key-spec mydb "state_pk")
   [:name :text]
   [:description :text]
   [:workflow_id :int "NOT NULL"]))     
       
(defn create-hooks
  "Create a table to store hooks"
  []
  (sql/create-table
   :hooks
   (get-id-key-spec mydb "hook_pk")
   [:name :text]
   [:code :text]
   [:action_id :int "NOT NULL"]))     
       
(defn create-tables 
	"Create all tables"
	[]
	(sql/with-connection
   mydb
   (clojure.contrib.sql/transaction
     	(create-workflows)
    	(create-actions)
    	(create-hooks))))
	
(defmacro with-ns "Execute a block within an existing namespace" 
	[my-ns form]
		`(let [original-ns *ns*]
			 (in-ns ~my-ns)
			 ~form
			 (in-ns original-ns)))
	 	
(defn create-models []
	 
	(ns vara.workflows
    (:require clj-record.boot))
	(clj-record.core/init-model 
		(:associations (has-many states)
									 (has-many actions)))
										 
	(ns vara.states
    (:require clj-record.boot))
	(clj-record.core/init-model)
										 
	(ns vara.actions
    (:require clj-record.boot))
	(clj-record.core/init-model
		(:associations (has-many hooks)))
			
	(ns vara.hooks
    (:require clj-record.boot))
	(clj-record.core/init-model)
		
	(ns user))
	