(ns workshop.data-oriented
  "Session 1b — Data-oriented programming (~15 min).

  One small vocabulary of functions, one universal data
  representation. No DTOs, no mappers, no TicketSummaryBuilder."
  (:require [clojure.repl :as repl]))

;; -----------------------------------------------------------------
;; The dataset: support tickets, as plain data.
;; This is also what an API response, a DB row, or an EDN file
;; looks like in Clojure — there is no 'mapping layer'.
;; -----------------------------------------------------------------

(def tickets
  [{:id 101 :status :open     :priority :high   :assignee "kari"  :tags #{:network :vpn}      :hours 3.5}
   {:id 102 :status :open     :priority :low    :assignee "jonas" :tags #{:printer}           :hours 0.5}
   {:id 103 :status :resolved :priority :high   :assignee "kari"  :tags #{:network :firewall} :hours 6.0}
   {:id 104 :status :open     :priority :medium :assignee "leah"  :tags #{:laptop :hardware}  :hours 1.0}
   {:id 105 :status :resolved :priority :low    :assignee "jonas" :tags #{:email}             :hours 0.25}
   {:id 106 :status :blocked  :priority :high   :assignee "kari"  :tags #{:vpn :vendor}       :hours 8.0}
   {:id 107 :status :open     :priority :medium :assignee "leah"  :tags #{:email :spam}       :hours 2.0}
   {:id 108 :status :resolved :priority :medium :assignee "jonas" :tags #{:laptop}            :hours 1.5}
   {:id 109 :status :open     :priority :high   :assignee "leah"  :tags #{:network}           :hours 4.0}
   {:id 110 :status :blocked  :priority :low    :assignee "kari"  :tags #{:printer :vendor}   :hours 0.75}])

;; -----------------------------------------------------------------
;; 1. Getting at data: keywords are functions, maps are functions
;; -----------------------------------------------------------------

(comment

  (first tickets)

  (:status (first tickets))       ; a keyword looks itself up

  (map :assignee tickets)
  (distinct (map :assignee tickets))

  ;; Destructuring — F# folks will feel at home:
  (let [{:keys [id status assignee]} (first tickets)]
    (str "#" id " is " (name status) " (" assignee ")"))

  ;; Sets are predicates too:
  (def my-filter (comp #{:open :blocked} :status))
  (filter my-filter tickets)
  )

;; -----------------------------------------------------------------
;; 2. The core vocabulary: filter / map / group-by / frequencies /
;;    sort-by / reduce — works on ALL data, because all data is values
;; -----------------------------------------------------------------

(comment

  (filter #(= :open (:status %)) tickets)

  (group-by :status tickets)

  (frequencies (map :priority tickets))

  (sort-by :hours > tickets)

  ;; Which tags show up most? (mapcat = flatMap/SelectMany/collect)
  (frequencies (mapcat :tags tickets))

  ;; Open high-priority hours — read it top to bottom:
  (->> tickets
       (filter #(= :open (:status %)))
       (filter #(= :high (:priority %)))
       (map :hours)
       (reduce +))
  )

;; -----------------------------------------------------------------
;; 3. Building reports = building data
;; -----------------------------------------------------------------

(defn workload
  "Hours of not-yet-resolved work per assignee."
  [tickets]
  (->> tickets
       (remove #(= :resolved (:status %)))
       (group-by :assignee)
       (map (fn [[who ts]]
              {:assignee who
               :open     (count ts)
               :hours    (reduce + (map :hours ts))}))
       (sort-by :hours >)))

(comment

  (workload tickets)

  ;; Change the fn above (add :top-priority), re-eval it, run again —
  ;; no restart. The report is just data: filter, sort, diff it.
  (map :assignee (workload tickets))
  )

;; -----------------------------------------------------------------
;; 4. Nested data, non-destructively
;; -----------------------------------------------------------------

(def infra
  {:datacenter "osl-1"
   :clusters {:web {:nodes 4 :version "1.31"}
              :db  {:nodes 2 :version "1.29"}}})

(comment

  (get-in infra [:clusters :web :nodes])
  (update-in infra [:clusters :web :nodes] + 2)
  (assoc-in  infra [:clusters :cache] {:nodes 1 :version "1.31"})

  infra   ;; still exactly what it was
  )
