(ns bmcgavin.birthday.db
  (:require [bmcgavin.birthday.db.mock :as db.mock]
            [bmcgavin.birthday.db.dynamodb :as db.dynamodb]))

(defn undbize [data & to-remove]
  (apply dissoc data to-remove))

(defmulti db-get :db-type)

(defmethod db-get :mock [data]
  (db.mock/db-get (undbize data :db-type :db-endpoint :aws)))

(defmethod db-get :dynamodb [data]
  (db.dynamodb/db-get (undbize data :db-type)))

(defmulti db-put :db-type)

(defmethod db-put :mock [data]
  (db.mock/db-put (undbize data :db-type  :db-endpoint :aws)))

(defmethod db-put :dynamodb [data]
  (db.dynamodb/db-put (undbize data :db-type)))