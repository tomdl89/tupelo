(ns build
  (:require
    [clojure.tools.build.api :as b]
    [deps-deploy.deps-deploy :as dd]
    ))

(def lib-name 'tupelo/tupelo) ; must be a qualified symbol
(def version "23.03.03f")
(def tag (str "v" version))
(def scm-root "github.com/cloojure/tupelo")

(def build-folder "target")
(def jar-content (str build-folder "/classes")) ; folder where we collect files to pack in a jar
(def basis (b/create-basis {:project "deps.edn"})) ; basis structure (read details in the article)
(def jar-file-name (format "%s/%s-%s.jar" build-folder (name lib-name) version)) ; path for result jar file

(defn clean-files [& args]
  (b/delete {:path build-folder})
  (println (format "Build folder \"%s\" removed" build-folder)))

(defn build-jar [& args]
  (clean-files) ; clean leftovers

  (b/copy-dir {:src-dirs   ["src/clj"
                            "src/cljc"
                            "resources"] ; prepare jar content
               :target-dir jar-content})

  (b/write-pom {:class-dir jar-content ; create pom.xml
                :lib       lib-name
                :version   version
                :basis     basis
                :src-dirs  ["src"]
                :scm       {:url                 (str "https://" scm-root)
                            :connection          (str "scm:git:git://" scm-root ".git")
                            :developerConnection (str "scm:git:ssh://git@" scm-root ".git")
                            :tag                 tag}})

  (b/jar {:class-dir jar-content ; create jar
          :jar-file  jar-file-name})
  (println (format "Jar file created: \"%s\"" jar-file-name)))

(defn deploy-clojars [& args]
  (build-jar)
  (dd/deploy {:installer :remote
              :artifact  jar-file-name
              :pom-file  (b/pom-path {:lib       lib-name
                                      :class-dir jar-content})}))
