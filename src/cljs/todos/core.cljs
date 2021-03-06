(ns todos.core
  (:require [crate.core :refer [html]]
            [domina :refer [append! by-id by-class set-html! log attrs value get-data]]
            [domina.css :refer [sel]]
            [domina.events :refer [listen! prevent-default target]])
  (:use-macros [crate.def-macros :only [defpartial]]))

(def id-seq (atom 0))

(defn make-task
  ([description]
     (make-task description "pending"))
  ([description state]
     {:id (swap! id-seq inc) :task description :state state}))

(def tasks (atom [(make-task "Learn ClojureScript" "done") (make-task "Profit")]))

(defpartial task-html [task]
  [:li {:id (:id task)
        :class (:state task)}
   [:a.task {:href "#" :data-task-id (:id task)} (:task task)]])

(defn render-all [tasks]
  (html [:div#content
         [:form#new-task {:action "#"}
          [:label {:for "description"} "Add A Task"]
          [:input {:name "description"}]]
         [:ul (map task-html tasks)]]))

(defn render-tasks [tasks]
  (set-html! (sel "ul")
             (map task-html tasks)))

(defn add-task [ev]
  (prevent-default ev)
  (swap! tasks conj (make-task (value (sel "input[name=description]")))))

(defn toggle-task [ev]
  (prevent-default ev)
  (let [id (js/parseInt (:data-task-id (attrs (target ev))))
        task (swap! tasks (fn [tasks]
                            (map (fn [task]
                                   (if (= id (:id task))
                                     (assoc task :state "done")
                                     task))
                                 tasks)))]))

(defn update-tasks-dom [key reference old-state new-state]
  (render-tasks @tasks))

(defn ^:export launch []
  (log "launching todos app")
  (append! (by-id "content") (render-all @tasks))
  (listen! :submit add-task)
  (listen! :click  toggle-task)
  (add-watch tasks :task-watcher update-tasks-dom))
