(ns bmcgavin.birthday.db.dynamodb-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [bmcgavin.birthday.db.dynamodb :as db.dynamodb]))


(def data {:aws {:aws-access-key-id "test"
                 :aws-secret-access-key "test"
                 :aws-region "us-east-1"}
           :db-endpoint-host "localhost"
           :db-endpoint-port "4566"
           :db-endpoint-protocol "http"})

(defn db-fixture [t]
  (db.dynamodb/db-put (merge data {:username "test" :birthday "2000-01-01"}))
  (t))

(use-fixtures :once db-fixture)

(deftest get-test
  (testing "dynamodb get"
    (is (= "2000-01-01" (db.dynamodb/db-get (merge data {:username "test"}))))
    (is (= nil (db.dynamodb/db-get (merge data {:username "invalid"}))))))

(deftest put-test
  (testing "dynamodb put"
    (db.dynamodb/db-put (merge data {:username "new-user" :birthday "2001-12-12"}))
    (is (= "2001-12-12" (db.dynamodb/db-get (merge data {:username "new-user"}))))))