(ns bmcgavin.birthday.api
  (:gen-class)
  (:require [org.httpkit.server :as http-server]
            [clojure.data.json :as json]
            [compojure.core :refer [defroutes POST GET PUT]]
            [compojure.route :as route]
            [clojure.tools.logging :as l]

            [fierycod.holy-lambda.core :as h]
            [fierycod.holy-lambda-ring-adapter.core :as hlra]
            [bmcgavin.birthday.date :refer [days-until-date valid?]]
            [bmcgavin.birthday.db :refer [db-get db-put]]))

(set! *warn-on-reflection* true)

(def config (atom nil))

(defn fetch-config []
  (l/debug "fetch-config")
  (let [db-endpoint-host-env (or (System/getenv "DB_ENDPOINT_HOST") "localhost")
        db-endpoint-host (cond
                           (= \$ (first db-endpoint-host-env))
                           (System/getenv (apply str (drop 1 db-endpoint-host-env)))
                           :else
                           db-endpoint-host-env)
        db-endpoint-protocol (or (System/getenv "DB_ENDPOINT_PROTOCOL") "https")
        db-endpoint-port (or (System/getenv "DB_ENDPOINT_PORT") "4566")
        db-type (keyword (or (System/getenv "DB_TYPE") "dynamodb"))
        db-endpoint (str db-endpoint-protocol "://" db-endpoint-host ":" db-endpoint-port)
        aws-access-key-id (or (System/getenv "AWS_ACCESS_KEY_ID") "")
        aws-secret-access-key (or (System/getenv "AWS_SECRET_ACCESS_KEY") "")
        aws-region (or (System/getenv "AWS_REGION") "")]
    (reset! config {:db-endpoint db-endpoint
                    :db-endpoint-protocol db-endpoint-protocol
                    :db-endpoint-host db-endpoint-host
                    :db-endpoint-port db-endpoint-port
                    :db-type db-type
                    :aws {:aws-access-key-id aws-access-key-id
                          :aws-secret-access-key aws-secret-access-key
                          :aws-region aws-region}})))

(defn bad-request [message]
  (l/debug "bad-request")
  {:status 400
   :headers {"Content-Type" "application/json"}
   :body (json/write-str {:message message})})

(defn success [message]
  (l/debug "success")
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/write-str {:message message})})

(defn dbize [data]
  (when (nil? @config)
    (fetch-config))
  (merge data @config))

(defn put [username payload]
  (l/debug "put")
  (let [birthday (:dateOfBirth payload)
        valid (valid? birthday)]
    (if valid
      (let [response (db-put (dbize {:username username :birthday birthday}))]
        (println response)
        (if (empty? response)
          {:status 204
           :headers {}}
          (bad-request response)))
      (bad-request "invalid dateOfBirth"))))

(defn put-handler [{:keys [params body]}]
  (l/debug "put-handler")
  (if (nil? body)
    (bad-request "no request body")
    (let [username (:username params)
          payload (json/read-str (slurp body) :key-fn keyword)]
      (cond
        (or (nil? payload) (nil? username))
        (bad-request "invalid request body or user found in path")
        :else
        (put username payload)))))

(defn get-handler [{:keys [params]}]
  (l/debug "get-handler")
  (let [username (:username params)
        birthday (db-get (dbize {:username username}))
        days (cond
               (nil? birthday)
               -1
               :else
               (days-until-date birthday))]
    (cond
      (= days -1)
      (success (str "Hello, " username "! I don't know when your birthday is :'("))
      (= days 0)
      (success (str "Hello, " username "! Happy birthday!"))
      :else
      (success (str "Hello, " username "! Your birthday is in " days " day(s)")))))

(defroutes app-routes
  (POST "/hello/:username" [] get-handler)
  (GET "/hello/:username" [] get-handler) ; duplicated due to lambda's post invocation requirement
  (PUT "/hello/:username" [] put-handler)
  (route/not-found bad-request))

;; AWS lambda proxying
(def LambdaEntrypoint (hlra/ring<->hl-middleware app-routes))

(h/entrypoint [#'LambdaEntrypoint])

;; The below is for running locally (REPL only for mock DB)
(defonce server (atom nil))

(defn stop-server []
  (l/info "shutting down")
  (when-not (nil? @server)
    (@server :timeout 1000)
    (reset! server nil)))

(defn app [& args]
  (reset! server (http-server/run-server #'app-routes {:port 8080}))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-server))
  (l/info (str "running")))
