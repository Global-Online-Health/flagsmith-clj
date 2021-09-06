(ns flagsmith-clj.core-test
  (:require
    [clj-wiremock.core :as wmk]
    [clojure.test :refer :all]
    [flagsmith-clj.core :as flagsmith]
    [flagsmith-clj.utils :as utils]
    [freeport.core :refer [get-free-port!]]
    [jsonista.core :as json])
  (:import
    (com.flagsmith
      FlagsmithClient)))


(def wiremock-port (get-free-port!))
(def wiremock-url (str "http://localhost:" wiremock-port))

(use-fixtures :once (partial wmk/wiremock-fixture {:port wiremock-port}))


(let [api-key "someKey"]
  (deftest initialization
    (testing "generates a new client"
      (is (= FlagsmithClient
             (type (flagsmith/new-client api-key)))))

    (testing "generates a new client with different base-uri"
      (is (= FlagsmithClient
             (type (flagsmith/new-client api-key {:base-uri wiremock-url}))))))

  (deftest has-feature
    (wmk/with-stubs
      [{:req [:GET (str "/flags/?page=1")]
        :res [200 {:body (json/write-value-as-string [(utils/create-feature :existing-feature true)])}]}]
      (let [client (flagsmith/new-client api-key {:base-uri wiremock-url})]
        (testing "has feature"
          (is (true? (flagsmith/has-feature client :existing-feature))))

        (testing "does not have feature"
          (is (false? (flagsmith/has-feature client :missing-feature))))))))
