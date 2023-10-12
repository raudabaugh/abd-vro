import logging
from typing import Callable

from base_queue_client import BaseQueueClient, Type
from hoppy_properties import ExchangeProperties, QueueProperties


class AsyncConsumer(BaseQueueClient):

    def __init__(self, config: [dict | None] = None,
                 exchange_properties: ExchangeProperties = ExchangeProperties(),
                 queue_properties: QueueProperties = QueueProperties(),
                 routing_key: str = '',
                 reply_callback: Callable = None,
                 prefetch_count: int = 1):
        super().__init__(Type.CONSUMER, config, exchange_properties, queue_properties, routing_key)

        self._consuming = False
        self._consumer_tag = None
        self._prefetch_count = prefetch_count
        self.reply_callback = reply_callback

    def _initialize_connection_session(self):
        """The following attributes are used per connection session. When a reconnect happens, they should be reset."""

        super()._initialize_connection_session()

        self._consuming = False

    def _ready(self):
        """Executed when the exchange and queue are ready and this class can start the process of consuming.
        Overrides super class abstract method."""
        logging.debug(f'event=specifyingQualityOfService '
                      f'client_type={self._client_type} '
                      f'prefetch_count={self._prefetch_count}')
        self._channel.basic_qos(prefetch_count=self._prefetch_count, callback=self._on_basic_qos_ok)

    def _shut_down(self):
        """Called when the client is requested to stop.
        Overrides super class abstract method"""
        if self._consuming:
            self._stop_consuming()

    def _on_basic_qos_ok(self, _unused_frame):
        logging.debug(f'event=specifiedQualityOfService '
                      f'client_type={self._client_type} '
                      f'prefetch_count={self._prefetch_count}')
        self._start_consuming()

    def _start_consuming(self):
        self._channel.add_on_cancel_callback(self._on_consumer_cancelled)
        self._consumer_tag = self._channel.basic_consume(self.queue_name, self._on_message)
        self._consuming = True
        logging.debug(f'event=startConsuming '
                      f'client_type={self._client_type} '
                      f'consumer_tag={self._consumer_tag}')

    def _on_consumer_cancelled(self, method_frame):
        logging.debug(f'event=serverCancelledConsumer '
                      f'client_type={self._client_type} '
                      f'method_frame={method_frame}')
        super()._close_channel()

    def _stop_consuming(self):
        if self._channel:
            logging.debug(f'event=stoppingConsuming '
                          f'client_type={self._client_type} '
                          f'consumer_tag={self._consumer_tag}')
            self._channel.basic_cancel(self._consumer_tag, self._on_cancel_ok)

    def _on_cancel_ok(self, _unused_frame):
        self._consuming = False
        logging.debug(f'event=stoppedConsuming '
                      f'client_type={self._client_type} '
                      f'consumer_tag={self._consumer_tag}')
        self._close_channel()

    def _on_message(self, _unused_channel, basic_deliver, properties, body):
        logging.debug(f'event=receivedMessage '
                      f'client_type={self._client_type} '
                      f'app_id={properties.app_id} '
                      f'delivery_tag={basic_deliver.delivery_tag} '
                      f'correlation_id={properties.correlation_id}')
        if self.reply_callback is not None:
            try:
                self.reply_callback(self._channel,
                                    properties,
                                    basic_deliver.delivery_tag,
                                    body)
            except Exception as e:
                logging.error(f'event=couldNotCallConsumeCallback '
                              f'client_type={self._client_type} '
                              f'callback={self.reply_callback} '
                              f'err={e}')

    def acknowledge_message(self, properties, delivery_tag):
        logging.debug(f'event=ackedMessage '
                      f'client_type={self._client_type} '
                      f'delivery_tag={delivery_tag} '
                      f'correlation_id={properties.correlation_id}')
        self._channel.basic_ack(delivery_tag)

    def reject_message(self, properties, delivery_tag, requeue=True):
        logging.debug(f'event=rejectedMessage '
                      f'client_type={self._client_type} '
                      f'delivery_tag={delivery_tag} '
                      f'correlation_id={properties.correlation_id} '
                      f'requeue={requeue}')
        self._channel.basic_reject(delivery_tag, requeue)
