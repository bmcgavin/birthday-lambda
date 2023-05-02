(ns bmcgavin.birthday.api-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [cljc.java-time.local-date :as ld]
            [clojure.data.json :as json]

            [bmcgavin.birthday.api :as api]
            [bmcgavin.birthday.db.mock :as db.mock]))

(def now (ld/now))
(def yesterday (-> now
                   (ld/minus-days 1)))
(def tomorrow (-> now
                  (ld/minus-years 1)
                  (ld/plus-days 1)))

(defn db-fixture [t]
  (reset! db.mock/store {:today (.toString now)
                         :yesterday (.toString yesterday)
                         :tomorrow (.toString tomorrow)})
  (t)
  (reset! db.mock/store {}))

(defn api-fixture [t]
  (reset! api/config {:db-type :mock
                      :db-endpoint :unused})
  (t)
  (reset! api/config {}))

(use-fixtures :each db-fixture api-fixture)

(deftest get-handler-test
  (testing "get handler"
    (let [response (api/get-handler {:params {:username "today"}})
          body (json/read-str (:body response) :key-fn keyword)]
      (is (= "Hello, today! Happy birthday!" (:message body))))
    (let [response (api/get-handler {:params {:username "yesterday"}})
          body (json/read-str (:body response) :key-fn keyword)]
      (is (= "Hello, yesterday! Your birthday is in 365 day(s)" (:message body))))
    (let [response (api/get-handler {:params {:username "tomorrow"}})
          body (json/read-str (:body response) :key-fn keyword)]
      (is (= "Hello, tomorrow! Your birthday is in 1 day(s)" (:message body))))))