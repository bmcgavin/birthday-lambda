(ns bmcgavin.birthday.db.dynamodb
  (:require [cognitect.aws.client.api :as aws]
            [cognitect.aws.protocols.ec2]
            [cognitect.aws.protocols.json]
            [cognitect.aws.protocols.query]
            [cognitect.aws.protocols.rest]
            [cognitect.aws.protocols.rest-xml]
            [cognitect.aws.protocols.rest-json]

            [fierycod.holy-lambda.agent :as agent]
            [cognitect.aws.http.cognitect :as http-client]))

(defn make-ddb
  ([] (delay (aws/client {:api :dynamodb :http-client (http-client/create)})))
  ([data] (delay
            (aws/client {:api                  :dynamodb
                         :region               (get-in data [:aws :aws-region])
                         :endpoint-override {:protocol (keyword (:db-endpoint-protocol data))
                                             :hostname (:db-endpoint-host data)
                                             :port     (Integer/parseInt (:db-endpoint-port data))}}))))

(defn db-get [data]
  (let [ddb (make-ddb data)
        response (aws/invoke @ddb
                             {:op :GetItem
                              :request {:TableName "birthday"
                                        :Key {:username {:S (:username data)}}}})]
    (if (empty? response)
      nil
      (get-in response [:Item :birthday :S]))))

(defn db-put [data]
  (let [ddb (make-ddb data)]
    (try (aws/invoke @ddb
                     {:op :PutItem
                      :request {:TableName "birthday"
                                :Item {:username {:S (:username data)}
                                       :birthday {:S (:birthday data)}}}})
         (catch Exception e (.getMessage e)))))

;; agent to aid with native image compilation
(agent/in-context
 (aws/invoke (deref (make-ddb)) {:op :ListTables}))