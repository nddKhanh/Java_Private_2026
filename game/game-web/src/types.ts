export interface Position {
  x: number;
  y: number;
}

export interface Team {
  id: string;
  displayName: string;
  color: string;
  resources: number;
}

export interface Unit {
  id: string;
  displayName: string;
  teamId: string;
  type: string;
  position: Position;
  hp: number;
  facing: 'UP' | 'RIGHT' | 'DOWN' | 'LEFT';
}

export interface Headquarters {
  id: string;
  teamId: string;
  position: Position;
  hp: number;
}

export interface ResourceNode {
  id: string;
  position: Position;
  amount: number;
}

export interface Building {
  id: string;
  teamId: string;
  type: 'BARRACKS' | 'TOWER' | 'WALL';
  position: Position;
  hp: number;
}

export interface MatchState {
  matchId: string;
  setupId: string;
  rulesVersion: string;
  status: 'RUNNING' | 'FINISHED';
  winnerTeamId: string | null;
  revision: number;
  eventSequence: number;
  mapWidth: number;
  mapHeight: number;
  teams: Team[];
  units: Unit[];
  retiredUnitIds: string[];
  headquarters: Headquarters[];
  resourceNodes: ResourceNode[];
  buildings: Building[];
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

export interface StateUpdatedMessage {
  type: 'STATE_UPDATED';
  schemaVersion: number;
  matchId: string;
  revision: number;
  sequence: number;
  state: MatchState;
  event: GameEvent | null;
}
