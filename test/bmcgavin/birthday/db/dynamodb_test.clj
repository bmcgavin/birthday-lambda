(ns bmcgavin.birthday.db.dynamodb-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [dynamodb.api :as api]
            [dynamodb.constant :as const]
            [bmcgavin.birthday.db.dynamodb :as db.dynamodb]))

(defn db-fixture [t]
  (do
    (api/create-table db.dynamodb/ddb
                      "birthday"
                      {:username :S}
                      {:username const/key-type-hash}
                      {:table-class const/table-class-standard
                       :billing-mode const/billing-mode-pay-per-request})
    (db.dynamodb/db-put {:username "test" :birthday "2000-01-01"}))
  (t)
  (api/delete-table db.dynamodb/ddb
                    "birthday"))

(use-fixtures :once db-fixture)

(deftest get-test
  (testing "dynamodb get"
    (is (= "2000-01-01" (db.dynamodb/db-get {:username "test"})))
    (is (= nil (db.dynamodb/db-get {:username "invalid"})))))

(deftest put-test
  (testing "dynamodb put"
    (is (= nil (db.dynamodb/db-get {:username "new-user"})))
    (db.dynamodb/db-put {:username "new-user" :birthday "2001-12-12"})
    (is (= "2001-12-12" (db.dynamodb/db-get {:username "new-user"})))))