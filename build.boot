(def +project+ 'mrmcc3/boot-pegdown)
(def +version+ "0.1.0-SNAPSHOT")

(set-env! :resource-paths #{"src"})

(def snapshot? (.endsWith +version+ "-SNAPSHOT"))

(task-options!
  pom {:project     +project+
       :version     +version+
       :description "Boot task that renders markdown files using pegdown"
       :url         "https://github.com/mrmcc3/boot-pegdown"
       :scm         {:url "https://github.com/mrmcc3/boot-pegdown"}
       :license     {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}}
 push {:repo "clojars"
       :ensure-clean true
       :tag (not snapshot?)
       :gpg-sign (not snapshot?)})

(require '[mrmcc3.boot-pegdown :refer [pegdown]])

(deftask build []
  (comp (pom) (jar) (install)))

(deftask deploy []
  (comp (build) (push)))
