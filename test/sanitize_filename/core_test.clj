(ns sanitize-filename.core-test
  (:require [sanitize-filename.core :as sut]
            [clojure.test :refer :all]
            [clojure.string :as str]))

(deftest sanitize-test
  (testing "replaces reserved Windows names with default"
    (is (= "file"
           (sut/sanitize "CON")))
    (is (= "file"
           (sut/sanitize "COM1")))
    (is (= "file"
           (sut/sanitize "prn")))
    (is (= "file"
           (sut/sanitize "LPT2")))
    (is (= "file"
           (sut/sanitize "AUX")))
    (is (= "file"
           (sut/sanitize "NUL")))
    ;; (is (= "file" ; https://msdn.microsoft.com/en-us/library/aa365247(v=vs.85).aspx#naming_conventions
    ;;        (sut/sanitize "NUL.txt")))
    )

  (testing "replaces invalid characters"
    (is (= "$"
           (sut/sanitize "/")))
    (is (= "$"
           (sut/sanitize "?")))
    (is (= "$"
           (sut/sanitize "<")))
    (is (= "$"
           (sut/sanitize ">")))
    (is (= "$"
           (sut/sanitize "\\")))
    (is (= "$"
           (sut/sanitize ":")))
    (is (= "$"
           (sut/sanitize "*")))
    (is (= "$"
           (sut/sanitize "|")))
    (is (= "$"
           (sut/sanitize "\"")))
    (is (= "file..$file$$.$$"
           (sut/sanitize "../file/>.*\"")))
    (is (= "$.pdf"
           (sut/sanitize "<.pdf"))))

  (testing "replaces control characters"
    (is (= "$"
           (sut/sanitize "\u0000")))
    (is (= "$"
           (sut/sanitize "\u001b")))
    (is (= "a$b"
           (sut/sanitize "a\u001cb")))
    (is (= "a$b"
           (sut/sanitize "a\u001fb")))
    (is (= "file"
           (sut/sanitize "\u001f"))) ; due to str/trim
    (is (= "$" ; https://en.wikipedia.org/wiki/C0_and_C1_control_codes
           (sut/sanitize "\u0080")))
    (is (= "$"
           (sut/sanitize "\u009f"))))

  (testing "replaces whitespace"
    (is (= "ab"
           (sut/sanitize "a b")))
    (is (= "ab"
           (sut/sanitize "a\tb")))
    (is (= "ab"
           (sut/sanitize "a\nb")))
    (is (= "ab"
           (sut/sanitize "a\fb")))
    (is (= "ab"
           (sut/sanitize "a\rb")))
    (is (= "ab"
           (sut/sanitize "a\u000bb")))
    (is (= "a"
           (sut/sanitize " a ")))
    (is (= "a"
           (sut/sanitize "\ta\n"))))

  (testing "extends reserved Unix names with default"
    (is (= "file$"
           (sut/sanitize ".")))
    (is (= "file$$"
           (sut/sanitize ".."))))

  (testing "handles invalid trailing chars for Windows"
    (is (= "name$"
           (sut/sanitize "name.")))
    (is (= "name$$"
           (sut/sanitize "name..")))
    (is (= "name"
           (sut/sanitize "name "))))

  (testing "trims long names"
    (let [name-with-n-chars (fn [n] (str/join "" (take n (repeat "x"))))]
      (is (= (name-with-n-chars 254)
             (sut/sanitize (name-with-n-chars 255)))))
    ;; (let [multi-byte-chars (str/join "" (take 254 (repeat "☃")))]
    ;;   (is (> 255
    ;;          (-> (sut/sanitize multi-byte-chars)
    ;;              (.getBytes "UTF-8")
    ;;              count))))
    )

  (testing "replaces empty name with default"
    (is (= "file"
           (sut/sanitize ""))))

  (testing "keeps valid names"
    (is (= "valid.mp3"
           (sut/sanitize "valid.mp3")))
    ;; (is (= ".valid"
    ;;        (sut/sanitize ".valid")))
    (is (= "valid"
           (sut/sanitize "valid")))
    (is (= "LPT10"
           (sut/sanitize "LPT10")))
    (is (= "笊,ざる.pdf"
           (sut/sanitize "笊,ざる.pdf")))))
