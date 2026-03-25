(ns mcp-server-sample.example-client
  "Minimal example of connecting to an MCP server using the plumcp library.

  Two transports are shown:
    1. HTTP (Streamable HTTP) — connect to a remote MCP server via URL
    2. STDIO               — spawn a local process as an MCP server

  Usage (HTTP):
    (connect-http! \"http://localhost:5551/mcp\")

  Usage (STDIO):
    (connect-stdio! \"npx\" [\"-y\" \"@modelcontextprotocol/server-filesystem\" \"/tmp\"])"
  (:require
   [plumcp.core.api.entity-support :as pes]
   [plumcp.core.api.mcp-client :as pmc]
   [plumcp.core.client.http-client-transport :as phct]
   [plumcp.core.client.stdio-client-transport :as psct]
   [plumcp.core.support.http-client :as phc]))

;; ---------------------------------------------------------------------------
;; HTTP (Streamable HTTP) transport
;; ---------------------------------------------------------------------------

(defn connect-http!
  "Connect to an MCP server over Streamable HTTP.
  Returns the initialized client."
  [url]
  (let [http-client (phc/make-http-client url {})
        transport   (phct/make-streamable-http-transport http-client)
        client      (pmc/make-mcp-client
                     {:info              (pes/make-info "my-client" "1.0.0" "My MCP Client")
                      :client-transport  transport
                      :print-banner?     false})]
    (pmc/initialize-and-notify! client {:timeout-millis 10000})
    client))

;; ---------------------------------------------------------------------------
;; STDIO transport
;; ---------------------------------------------------------------------------

(defn connect-stdio!
  "Spawn `command` with `args` as an MCP server process and connect to it.
  Returns the initialized client."
  [command args]
  (let [transport (psct/run-command
                   {:command-tokens (into [command] args)
                    :dir            (System/getProperty "user.home")})
        client    (pmc/make-mcp-client
                   {:info             (pes/make-info "my-client" "1.0.0" "My MCP Client")
                    :client-transport transport
                    :print-banner?    false})]
    (pmc/initialize-and-notify! client {:timeout-millis 10000})
    client))

;; ---------------------------------------------------------------------------
;; Using the client
;; ---------------------------------------------------------------------------

(comment
  ;; -- HTTP example --
  (def client (connect-http! "http://localhost:5551/mcp"))

  ;; Inspect the initialize result (capabilities, server info)
  (pmc/get-initialize-result client)

  ;; List available tools
  (pmc/list-tools client {})

  ;; Call a tool
  (pmc/call-tool client "echo" {"message" "hello"})

  ;; List prompts
  (pmc/list-prompts client {})

  ;; Disconnect
  (pmc/disconnect! client)

  ;; -- STDIO example --
  (def client (connect-stdio! "npx" ["-y" "@modelcontextprotocol/server-filesystem" "/tmp"]))
  (pmc/list-tools client {})
  (pmc/disconnect! client))
