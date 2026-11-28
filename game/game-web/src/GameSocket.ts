import type { StateUpdatedMessage } from './types';

export const SCHEMA_VERSION = 9;

export type ConnectionStatus = 'connecting' | 'online' | 'offline';

export class GameSocket {
  private socket: WebSocket | null = null;
  private reconnectTimer: number | null = null;
  private disposed = false;

  constructor(
    private readonly url: string,
    private readonly onStatus: (status: ConnectionStatus) => void,
    private readonly onState: (message: StateUpdatedMessage) => void,
  ) {}

  connect(): void {
    this.disposed = false;
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
      if (!this.disposed) this.scheduleReconnect();
    });
  }

  /** Sends a message when the socket is open; returns false otherwise. */
  send(message: Record<string, unknown>): boolean {
    if (this.socket?.readyState !== WebSocket.OPEN) return false;
    this.socket.send(JSON.stringify({ schemaVersion: SCHEMA_VERSION, ...message }));
    return true;
  }

  disconnect(): void {
    this.disposed = true;
    if (this.reconnectTimer !== null) {
      window.clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
    this.socket?.close();
    this.socket = null;
  }

  private receive(raw: unknown): void {
    if (this.disposed) return;
    if (typeof raw !== 'string') {
      return;
    }
    let message: Partial<StateUpdatedMessage>;
    try {
      message = JSON.parse(raw) as Partial<StateUpdatedMessage>;
    } catch {
      // Ignore malformed messages and keep the last confirmed snapshot visible.
      return;
    }
    if (message.type === 'STATE_UPDATED' && message.schemaVersion === SCHEMA_VERSION && message.state) {
      try {
        this.onState(message as StateUpdatedMessage);
      } catch (error) {
        // A rendering bug must not hide later states, but it should still be visible in the console.
        console.error('Không hiển thị được trạng thái trận', error);
      }
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
