(ns bmcgavin.birthday.db-test
  (:require [clojure.test :refer [deftest is testing]]
            [bmcgavin.birthday.db :as db]))

(deftest get-test
  (testing "Mock get"
    (is (= nil (db/db-get {:db-type :mock :username "invalid"})))))

(deftest put-test
  (testing "Mock put"
    (is (= nil (db/db-get {:db-type :mock :username "new-user"})))
    (db/db-put {:db-type :mock :username "new-user" :birthday "2001-12-12"})
    (is (= "2001-12-12" (db/db-get {:db-type :mock :username "new-user"})))))