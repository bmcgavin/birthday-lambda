(ns bmcgavin.birthday.db.dynamodb
  (:require [cognitect.aws.client.api :as aws]
            [cognitect.aws.credentials :as credentials]
            [fierycod.holy-lambda.agent :as agent]))

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
    (make-ddb data))
  (let [response (aws/invoke @ddb
                             {:op :GetItem
                              :request {:TableName "birthday"
                                        :Key {:username {:S (:username data)}}}})]
    (if (empty? response)
      nil
      (get-in response [:Item :birthday :S]))))

(defn db-put [data]
  (when (nil? @ddb)
    (make-ddb data))
  (let [response (aws/invoke @ddb
                             {:op :PutItem
                              :request {:TableName "birthday"
                                        :Item {:username {:S (:username data)}
                                               :birthday {:S (:birthday data)}}}})]
    (any? response)))

;; agent to aid with native image compilation
(agent/in-context
 (let [client (aws/client {:api                  :dynamodb
                           :region               "us-east-1"
                           :credentials-provider (credentials/basic-credentials-provider
                                                  {:access-key-id     "test"
                                                   :secret-access-key "test"})
                           :endpoint-override {:protocol :http
                                               :hostname "localhost"
                                               :port     4566}})
       response (aws/invoke client {:op :ListTables})]
   (println response)))