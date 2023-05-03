(ns bmcgavin.birthday.date
  (:require [cljc.java-time.local-date :as ld]
            [cljc.java-time.duration :as d]))

(defn valid? [date]
  (try
    (ld/parse date)
    (catch Exception _ nil)))

(defn days-until-date
  [date & args]
  (let [now (if (nil? args) (.toString (ld/now)) (first args))
        now-year (-> now
                     ld/parse
                     ld/get-year)
        now-date (-> now
                     ld/parse
                     ld/at-start-of-day)
        date-this-year (-> date
                           ld/parse
                           (ld/with-year now-year)
                           ld/at-start-of-day)
        days (.toDays (d/between now-date date-this-year))]
    (cond
      (>= days 0)
      days
      :else
      (let [date-next-year (-> date
                               ld/parse
                               (ld/with-year (+ 1 now-year))
                               ld/at-start-of-day)]
        (.toDays (d/between now-date date-next-year))))))
