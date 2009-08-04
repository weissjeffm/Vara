(use 'clojure.contrib.sql)
 

 
(def mydb {:classname "org.postgresql.Driver" ; must be in classpath
         :subprotocol "postgresql"
         :subname (str "//localhost:5432/vara")
         ; Any additional keys are passed to the driver
         ; as driver-specific properties.
         :user "weissj"
         :password "dog8code"})
         
 (defn create-bugs
  "Create a table to store bug entries"
  []
  (clojure.contrib.sql/create-table
   :bugs
   [:id :serial "primary key"]
   [:headline :text]
   [:description :text]))
         
(defn create-tables 
	"Create all tables"
	[]
	(clojure.contrib.sql/with-connection
   mydb
   (clojure.contrib.sql/transaction
     (create-bugs))))
	