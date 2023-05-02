(ns bmcgavin.birthday.db.mock)

(defonce store (atom {}))

(defn db-get [data]
  ((keyword (:username data)) @store))

(defn db-put [data]
  (swap! store assoc (keyword (:username data)) (:birthday data)))