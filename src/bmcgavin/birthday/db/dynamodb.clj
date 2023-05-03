;; (ns bmcgavin.birthday.db.dynamodb
;;   (:require [taoensso.faraday :as far]))

;; (defn make-client-opts [data]
;;   {:access-key (get-in data [:aws :aws-access-key-id])
;;    :secret-key (get-in data [:aws :aws-secret-access-key])
;;    :endpoint (:db-endpoint data)})

;; (defn clean [data]
;;   (apply dissoc data [:db-endpoint :aws]))

;; (def client-opts (atom nil))

;; (defn db-get [data]
;;   (when (nil? @client-opts)
;;     (reset! client-opts (make-client-opts data)))
;;   (let [item (far/get-item @client-opts
;;                            :birthday
;;                            (clean data))]
;;     (cond
;;       (nil? item)
;;       item
;;       :else
;;       (:birthday item))))

;; (defn db-put [data]
;;   (when (nil? @client-opts)
;;     (reset! client-opts (make-client-opts data)))
;;   (far/put-item @client-opts
;;                 :birthday
;;                 (clean data)))

;; (ns bmcgavin.birthday.db.dynamodb
;;   (:require [dynamodb.api :as api]))

;; (defn make-ddb [data]
;;   (let [aki (get-in data [:aws :aws-access-key-id])
;;         ask (get-in data [:aws :aws-secret-access-key])
;;         db (:db-endpoint data)
;;         region (get-in data [:aws :aws-region])]
;;     (api/make-client aki
;;                      ask
;;                      db
;;                      region)))

;; (defn clean [data]
;;   (apply dissoc data [:db-endpoint :aws]))

;; (def ddb (atom nil))

;; (defn db-get [data]
;;   (println (str "data in db-get: " data))
;;   (println (str "ddb: " @ddb))
;;   (when (nil? @ddb)
;;     (reset! ddb (make-ddb data)))
;;   (let [item (api/get-item ddb
;;                            "birthday"
;;                            (clean data))]
;;     (cond
;;       (nil? item)
;;       item
;;       :else
;;       (get-in item [:Item :birthday]))))

;; (defn db-put [data]
;;   (when (nil? ~ddb)
;;     (reset! ddb (make-ddb data)))
;;   (api/put-item @ddb
;;                 "birthday"
;;                 (clean data)))

(ns bmcgavin.birthday.db.dynamodb
  (:require [cognitect.aws.client.api :as aws]
            [cognitect.aws.credentials :as credentials]))

(def ddb (atom nil))

(defn make-ddb [data]
  (reset! ddb (aws/client {:api                  :dynamodb
                           :region               (get-in data [:aws :aws-region])
                           :credentials-provider (credentials/basic-credentials-provider
                                                  {:access-key-id     (get-in data [:aws :aws-access-key-id])
                                                   :secret-access-key (get-in data [:aws :aws-secret-access-key])})
                           :endpoint-override {:protocol (keyword (:db-endpoint-protocol data))
                                               :hostname (:db-endpoint-host data)
                                               :port     (Integer/parseInt (:db-endpoint-port data))}})))

(defn db-get [data]
  (when (nil? @ddb)
    (reset! ddb (make-ddb data)))
  (let [response (aws/invoke @ddb
                             {:op :GetItem
                              :request {:TableName "birthday"
                                        :Key {:username {:S (:username data)}}}})]
    (if (empty? response)
      nil
      (get-in response [:Item :birthday :S]))))

(defn db-put [data]
  (when (nil? @ddb)
    (reset! ddb (make-ddb data)))
  (let [response (aws/invoke @ddb
                             {:op :PutItem
                              :request {:TableName "birthday"
                                        :Item {:username {:S (:username data)}
                                               :birthday {:S (:birthday data)}}}})]
    (println response)
    (any? response)))