import type { StateUpdatedMessage } from './types';

export type ConnectionStatus = 'connecting' | 'online' | 'offline';

export class GameSocket {
  private socket: WebSocket | null = null;
  private reconnectTimer: number | null = null;

  constructor(
    private readonly url: string,
    private readonly onStatus: (status: ConnectionStatus) => void,
    private readonly onState: (message: StateUpdatedMessage) => void,
  ) {}

  connect(): void {
    if (this.socket?.readyState === WebSocket.OPEN || this.socket?.readyState === WebSocket.CONNECTING) {
      return;
    }
    this.onStatus('connecting');
    this.socket = new WebSocket(this.url);
    this.socket.addEventListener('open', () => this.onStatus('online'));
    this.socket.addEventListener('message', (event) => this.receive(event.data));
    this.socket.addEventListener('error', () => this.socket?.close());
    this.socket.addEventListener('close', () => {
      this.onStatus('offline');
      this.scheduleReconnect();
    });
  }

  private receive(raw: unknown): void {
    if (typeof raw !== 'string') {
      return;
    }
    try {
      const message = JSON.parse(raw) as Partial<StateUpdatedMessage>;
      if (message.type === 'STATE_UPDATED' && message.schemaVersion === 1 && message.state) {
        this.onState(message as StateUpdatedMessage);
      }
    } catch {
      // Ignore malformed messages and keep the last confirmed snapshot visible.
    }
  }

  private scheduleReconnect(): void {
    if (this.reconnectTimer !== null) {
      window.clearTimeout(this.reconnectTimer);
    }
    this.reconnectTimer = window.setTimeout(() => {
      this.reconnectTimer = null;
      this.connect();
    }, 2000);
  }
}
