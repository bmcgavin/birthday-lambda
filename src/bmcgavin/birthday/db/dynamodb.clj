(ns bmcgavin.birthday.db.dynamodb
  (:require [dynamodb.api :as api]))

(defn make-ddb [data]
  (println (str "data: " data))
  (println (str "aki: " (get-in data [:aws :aws-access-key-id])))
  (println (str "ask: " (get-in data [:aws :aws-secret-access-key])))
  (api/make-client (str (get-in data [:aws :aws-access-key-id]))
                   (str (get-in data [:aws :aws-secret-access-key]))
                   (:db-endpoint data)
                   (get-in data [:aws :aws-region])))

(defn clean [data]
  (apply dissoc data [:db-endpoint :aws]))

(def ddb (atom nil))

(defn db-get [data]
  (println (str "data in db-get: " data))
  (println (str "ddb: " @ddb))
  (when (nil? @ddb)
    (reset! ddb (make-ddb data)))
  (let [item (api/get-item ddb
                           "birthday"
                           (clean data))]
    (cond
      (nil? item)
      item
      :else
      (get-in item [:Item :birthday]))))

(defn db-put [data]
  (when (nil? ddb)
    (reset! ddb (make-ddb data)))
  (api/put-item ddb
                "birthday"
                (clean data)))