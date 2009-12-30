(ns vara.db.utils
  (:use clojure.contrib.str-utils))

(def mydb "vara")

(def datatypes #{:fieldTypeDef :field :user :group :recordTypeDef :record})

(defn valid-datatype? [kw]
  (contains? datatypes kw))

(defn validate-datatype [kw]
  (if-not (valid-datatype? kw)
    (throw (IllegalArgumentException. 
	    (format "Invalid db datatype %s, known types are %s" (str kw) (str datatypes))))))

(defn db-id "Puts together a couchdb document id from a datatype keyword and a string"
  [kw id-str]
  (validate-datatype kw)  
  (str (name kw) "-" id-str))

(defn db-type "Strips the type info off of a couchdb id, and returns a keyword."
  [db-id]
  (let [mytype (keyword (first (re-split #"-" db-id)))]
    (validate-datatype mytype)
    mytype))
