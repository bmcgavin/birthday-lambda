(ns bmcgavin.birthday.db.mock-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [bmcgavin.birthday.db.mock :as db.mock]))

(defn db-fixture [t]
  (reset! db.mock/store {:test "2000-01-01"})
  (t)
  (reset! db.mock/store {}))

(use-fixtures :once db-fixture)

(deftest get-test
  (testing "Mock get"
    (is (= "2000-01-01" (db.mock/db-get {:username "test"})))
    (is (= nil (db.mock/db-get {:username "invalid"})))))

(deftest put-test
  (testing "Mock put"
    (is (= nil (db.mock/db-get {:username "new-user"})))
    (db.mock/db-put {:username "new-user" :birthday "2001-12-12"})
    (is (= "2001-12-12" (db.mock/db-get {:username "new-user"})))))