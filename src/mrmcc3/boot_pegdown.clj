(ns mrmcc3.boot-pegdown
  {:boot/export-tasks true}
  (:require
    [boot.core :as core :refer [deftask]]
    [boot.pod :as pod]
    [boot.util :as util]))

(def ^:private deps '[[org.pegdown/pegdown "1.6.0"]])

(deftask pegdown
  [n no-cache bool]
  (let [p (-> (core/get-env)
              (update-in [:dependencies] into deps)
              pod/make-pod
              future)
        render-md
        (fn [m f]
          (let [content (-> f core/tmp-file slurp)
                md (pod/with-call-in @p
                     (mrmcc3.boot-pegdown.impl/md->html ~content [:all]))]
            (assoc-in m [(core/tmp-path f) ::html] md)))
        prev-fileset (atom nil)
        cached-html (atom {})]
    (core/with-pre-wrap fileset
      (let [prev @prev-fileset
            targets (->> (core/fileset-diff prev fileset :hash)
                         core/input-files
                         (core/by-ext ["md"]))
            removed (->> (core/fileset-removed prev fileset)
                         core/input-files
                         (core/by-ext ["md"])
                         (map core/tmp-path))
            new-html (reduce render-md {} targets)
            meta-map (apply dissoc (merge @cached-html new-html) removed)]
        (when (seq targets)
          (util/info "[pegdown] rendered %s files... %s cached.\n"
            (count targets) (- (count meta-map) (count targets))))
        (when-not no-cache
          (reset! prev-fileset fileset)
          (reset! cached-html meta-map))
        (core/add-meta fileset meta-map)))))
