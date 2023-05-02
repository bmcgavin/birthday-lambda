(ns bmcgavin.birthday.date-test
  (:require [clojure.test :refer [deftest is testing]]
            [bmcgavin.birthday.date :as date]))

(deftest days-until-date-test
  (testing "days-until-date"
    (is (= 0 (date/days-until-date "2023-04-30" "2023-04-30")))
    (is (= 1 (date/days-until-date "2023-05-01" "2023-04-30")))
    (is (= 364 (date/days-until-date "2022-04-29" "2023-04-30")))
    (is (= 365 (date/days-until-date "2023-04-29" "2023-04-30")))))
