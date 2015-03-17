import io.vertx.groovy.ext.apex.Router
import io.vertx.groovy.ext.apex.handler.sockjs.SockJSHandler
import io.vertx.groovy.ext.apex.handler.StaticHandler

// Create an apex {@link Router}
def router = Router.router(vertx)

// Get the cached EventBus instance from Vertx.
def eb = vertx.eventBus()

// Register a listener on the {@link EventBus} to recieve messages from the client.
eb.consumer("chat.to.server").handler({ message ->
  // When a message is recieved, prepend a timestamp and send the message back to all clients.
  def now = java.util.Date.from(java.time.Instant.now())
  def timestamp = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.MEDIUM).format(now)
  eb.publish("chat.to.client", "${timestamp}: ${message.body()}")
})

// Configure the {@link EventBus} bridge allowing only the specified addresses in/out.
def opts = [
  inboundPermitteds:[
    [
      address:"chat.to.server"
    ]
  ],
  outboundPermitteds:[
    [
      address:"chat.to.client"
    ]
  ]
]

def ebHandler = SockJSHandler.create(vertx).bridge(opts)
router.route("/eventbus/*").handler(ebHandler)

router.route().handler(StaticHandler.create("webroot/").setIndexPage("chat.html"))

vertx.createHttpServer().requestHandler(router.&accept).listen(8000)
