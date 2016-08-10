(ns sanitize-filename.core
  (:require [clojure.string :as s]))

(def CHARACTER_FILTER #"[\x00-\x1F\/\\:\*\?\"<>\|]")
(def UNICODE_WHITESPACE #"\p{Space}")
(def WINDOWS_RESERVED_NAMES #{"CON" "PRN" "AUX" "NUL" "COM1" "COM2" "COM3" "COM4" "COM5"
                              "COM6" "COM7" "COM8" "COM9" "LPT1" "LPT2" "LPT3" "LPT4"
                              "LPT5" "LPT6" "LPT7" "LPT8" "LPT9"})
(def RESERVED_NAMES #"^\.+$")
(def FALLBACK_FILENAME "file")

(defn- filter-windows-reserved-names [filename]
  (if (WINDOWS_RESERVED_NAMES (s/upper-case filename))
    "file"
    filename
    )
  )

(defn- filter-blank [filename]
  (if (s/blank? filename)
    FALLBACK_FILENAME
    filename
    )
  )

(defn- filter-reserved-names [filename]
  (if (re-matches RESERVED_NAMES filename)
    FALLBACK_FILENAME
    filename
    )
  )

(defn- -filter [filename]
  (-> filename
      filter-windows-reserved-names
      filter-reserved-names
      filter-blank
      )
  )

(defn- -sanitize [filename]
  (-> filename
      ;(s/replace CHARACTER_FILTER ""))
      ; NOTE: different with zaru
      ; replace with $, to indicate that there was a special character
      (s/replace CHARACTER_FILTER (s/re-quote-replacement "$")))
  )

(defn- truncate [filename]
  (if (> (.length filename) 254)
    (.substring filename 0 254)
    filename)
  )

;; exported function
(defn sanitize [filename]
  (-> filename
      -sanitize
      -filter
      truncate)
  )

;(defn -main []
;  should print "$a$b$  我是c.zip"
;  (println (sanitize "/a/b/  我是c.zip")))
