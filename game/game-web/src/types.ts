export interface Position {
  x: number;
  y: number;
}

export interface Team {
  id: string;
  displayName: string;
  color: string;
  /** Present only for the viewer's own team. */
  wood?: number;
  meat?: number;
}

export interface Unit {
  id: string;
  displayName: string;
  teamId: string;
  type: string;
  position: Position;
  hp: number;
  facing: 'UP' | 'RIGHT' | 'DOWN' | 'LEFT';
  width: number;
  height: number;
  /** HP the unit spawned with; the server sends it so health bars stay right when the rules change. */
  maxHp: number;
}

export interface Headquarters {
  id: string;
  teamId: string;
  position: Position;
  hp: number;
  maxHp: number;
}

export interface ResourceNode {
  id: string;
  type: 'WOOD' | 'MEAT';
  position: Position;
  amount: number;
}

export interface Building {
  id: string;
  teamId: string;
  type: 'BARRACKS' | 'TOWER' | 'WALL';
  position: Position;
  hp: number;
  maxHp: number;
}

export interface Projectile {
  id: string;
  teamId: string;
  sourceUnitId: string;
  targetId: string;
  position: Position;
  damage: number;
}

export interface MatchState {
  matchId: string;
  setupId: string;
  rulesVersion: string;
  status: 'RUNNING' | 'FINISHED';
  winnerTeamId: string | null;
  revision: number;
  eventSequence: number;
  simulationTick: number;
  mapWidth: number;
  mapHeight: number;
  teams: Team[];
  units: Unit[];
  retiredUnitIds: string[];
  headquarters: Headquarters[];
  resourceNodes: ResourceNode[];
  buildings: Building[];
  projectiles: Projectile[];
  processedCommandIds: string[];
}

export interface GameEvent {
  sequence: number;
  revision: number;
  commandId: string;
  action: string;
  actorTeamId: string;
  unitId: string;
  targetId: string | null;
}

/** One pending command in a bot's queue; target is the new unit or building id when there is one. */
export interface QueuedCommand {
  commandId: string;
  action: string;
  direction: string | null;
  type: string | null;
  target: string | null;
}

/** A command of the viewer's team that failed on its turn; droppedCount counts the later commands removed with it. */
export interface CommandRejection {
  batchId: string | null;
  commandId: string;
  subjectId: string;
  action: string | null;
  error: string;
  errorMessage: string | null;
  droppedCount: number;
}

export interface StateUpdatedMessage {
  type: 'STATE_UPDATED';
  schemaVersion: number;
  matchId: string;
  revision: number;
  sequence: number;
  /** The team this connection plays for, or null for a spectator. */
  viewerTeamId: string | null;
  state: MatchState;
  events: GameEvent[];
  /** Pending commands of the viewer's own bots, keyed by bot id. */
  queues: Record<string, QueuedCommand[]>;
  rejections: CommandRejection[];
}

export interface MatchRuntime {
  matchId: string;
  status: 'RUNNING' | 'FINISHED' | string;
  revision: number;
  simulationTick: number;
  lastTickMicros: number;
  maxTickMicros: number;
  tickSamples: number;
}

export interface Account {
  id: string;
  username: string;
  displayName: string;
  teamId: string | null;
}

export interface Session {
  token: string;
  expiresAt: string;
  account: Account;
}

export interface LobbyMember {
  accountId: string;
  username: string;
}

export interface LobbyTeam {
  teamId: string;
  displayName: string;
  color: string;
  token: string;
  capacity: number;
  members: LobbyMember[];
}

export interface LobbyView {
  matchCode: string;
  pairingKey: string;
  status: 'WAITING' | 'RUNNING';
  currentAccountId: string;
  owner: boolean;
  teams: LobbyTeam[];
}
