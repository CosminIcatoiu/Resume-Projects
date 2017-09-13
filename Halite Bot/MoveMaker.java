import java.lang.reflect.Array;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;

class MoveMaker {

	/*
	 * Variabila ce retine pentru fiecare celula directia in care va face
	 * mutarea si este folosita in sortarea topologica.
	 */

	public static Map<Location, Direction> parent;

	private static ArrayList<Location> stack;
	private static HashMap<Location, Boolean> visited;

	private static LinkedList<Location> q;
	private static HashMap<Location, Integer> distancesToEnemy;
	private static HashMap<Location, Integer> distancesToNeutral;
	private static HashMap<Location, Location> closestEnemy;
	private static HashMap<Location, Location> closestNeutral;

	/*
	 * Variabila retine pentru fiecare celula detinuta de noi, indicele acesteia
	 * obtinut in urma sortarii topologice.
	 */
	private static HashMap<Location, Integer> sortedIndex;

	private static int[] xStep = { -1, 0, 1, 0 };
	private static int[] yStep = { 0, -1, 0, 1 };

	/*
	 * Pentru fiecare celula detinuta de noi, functia va determina cea mai
	 * apropiata celula neutra si cea mai apropiata celula inamica. Acest lucru
	 * este dat de parametrul type (0 pentru celule neutre si 1 pentru celule
	 * inamice). Am modelat harta ca un graf in care fiecare celula avea muchie
	 * catre toate cele 4 celule vecine. Functia foloseste un multi-source bfs,
	 * in care sursele sunt ori toate celulele neutre ori toate celulele
	 * inamice.
	 */

	static void bfs(GameMap gameMap, int myID, int type) {

		ArrayList<Location> unConquered = new ArrayList<Location>();
		q = new LinkedList<Location>();

		if (type == 0) {
			distancesToNeutral = new HashMap<Location, Integer>();
			closestNeutral = new HashMap<Location, Location>();
		} else {
			distancesToEnemy = new HashMap<Location, Integer>();
			closestEnemy = new HashMap<Location, Location>();
		}

		for (int x = 0; x < gameMap.width; x++) {
			for (int y = 0; y < gameMap.height; y++) {
				final Location location = gameMap.getLocation(x, y);
				final Site site = location.getSite();

				if (type == 0) {
					if (site.owner == 0) {
						distancesToNeutral.put(location, 0);
						closestNeutral.put(location, location);

						unConquered.add(location);
					} else {
						distancesToNeutral.put(location, -1);
						closestNeutral.put(location, null);
					}
				} else {
					if (site.owner != myID && site.owner != 0) {
						distancesToEnemy.put(location, 0);
						closestEnemy.put(location, location);
						unConquered.add(location);
					} else {
						distancesToEnemy.put(location, -1);
						closestEnemy.put(location, null);
					}
				}
			}
		}

		if (type == 0) {
			Collections.sort(unConquered);
		} else {
			Collections.sort(unConquered, new LocationComparator(gameMap));
		}

		if (type == 0) {
			for (int i = 0; i < unConquered.size() / 4; i++) {
				q.add(unConquered.get(i));
			}
		} else {
			for (Location location : unConquered) {
				q.add(location);
			}
		}

		while (!q.isEmpty()) {
			Location first = q.poll();

			for (int i = 0; i < 4; i++) {
				int x = first.x;
				int y = first.y;

				x = (x + gameMap.width + xStep[i]) % gameMap.width;
				y = (y + gameMap.height + yStep[i]) % gameMap.height;

				Location next = new Location(x, y, null);

				if (type == 0) {
					if (distancesToNeutral.get(next) == -1) {
						distancesToNeutral.put(next,
								1 + distancesToNeutral.get(first));
						closestNeutral.put(next, closestNeutral.get(first));
						q.add(next);
					}
				} else {
					if (distancesToEnemy.get(next) == -1) {
						distancesToEnemy.put(next,
								1 + distancesToEnemy.get(first));
						closestEnemy.put(next, closestEnemy.get(first));
						q.add(next);
					}
				}
			}
		}
	}

	/*
	 * Metoda este folosita pentru celulele care au ca vecini doar celule
	 * detinute de noi. Este returnata directia in care acestea trebuie sa se
	 * deplaseze pentru a ajunge cat mai eficient la inamic sau la celula
	 * necucerita cea mai apropiata
	 */

	static Direction findDirection(Location currentLocation, GameMap gameMap) {

		int x = currentLocation.x;
		int y = currentLocation.y;

		Pair enemy = closestEnemy(gameMap, currentLocation,
				currentLocation.getSite().owner);
		Pair neutral = closestNeutral(gameMap, currentLocation,
				currentLocation.getSite().owner);

		if (!isSurroundedByFriendlyCells(gameMap, currentLocation,
				currentLocation.getSite().owner)
				|| (enemy.getDistance() == Integer.MAX_VALUE && neutral
						.getDistance() == Integer.MAX_VALUE)
				|| (neutral.getDistance() >= distancesToNeutral
						.get(currentLocation) + 4)) {
			for (int i = 0; i < 4; i++) {
				int x1 = (x + gameMap.width + xStep[i]) % gameMap.width;
				int y1 = (y + gameMap.height + yStep[i]) % gameMap.height;

				if (distancesToNeutral.get(currentLocation) + 7 < distancesToEnemy
						.get(currentLocation)) {
					if (distancesToNeutral.get(new Location(x1, y1, null)) == distancesToNeutral
							.get(currentLocation) - 1) {

						if (i == 0) {
							return Direction.WEST;
						}
						if (i == 1) {
							return Direction.NORTH;
						}
						if (i == 2) {
							return Direction.EAST;
						}
						return Direction.SOUTH;
					}
				} else {
					if (distancesToEnemy.get(new Location(x1, y1, null)) == distancesToEnemy
							.get(currentLocation) - 1) {
						if (i == 0) {
							return Direction.WEST;
						}
						if (i == 1) {
							return Direction.NORTH;
						}
						if (i == 2) {
							return Direction.EAST;
						}
						return Direction.SOUTH;
					}
				}
			}

			return Direction.EAST;
		} else {

			if (neutral.getDistance() + 9 < enemy.getDistance()) {
				return neutral.getDirection();
			} else {
				return enemy.getDirection();
			}
		}
	}

	/*
	 * Realizeaza o parcurgere in adancime a unui graf, pornind de la o anumita
	 * locatie
	 */

	static void dfs(Location location, GameMap gameMap) {

		visited.put(location, true);

		if (parent.get(location) != null) {
			Direction dir = parent.get(location);
			int x = location.x;
			int y = location.y;
			if (dir == Direction.WEST) {
				x = x == 0 ? gameMap.width - 1 : x - 1;
			} else if (dir == Direction.NORTH) {
				y = y == 0 ? gameMap.height - 1 : y - 1;
			} else if (dir == Direction.EAST) {
				x = x == gameMap.width - 1 ? 0 : x + 1;
			} else if (dir == Direction.SOUTH) {
				y = y == gameMap.height - 1 ? 0 : y + 1;
			}

			Location next = gameMap.getLocation(x, y);
			if (visited.get(next) != null) {
				if (!visited.get(next))
					dfs(next, gameMap);
			}
		}

		stack.add(location);
	}

	/*
	 * Returneaza o lista ce contine celulele detinute de noi, in ordinea in
	 * care acestea trebuie sa efectueze mutarile, ordine ce garanteaza evitarea
	 * risipei de productie. Graful care este sortat topologic contine toate
	 * celulele detinute de noi, fiecare celula avand o singura muchie, catre
	 * celula peste care urmeaza sa se mute, conform directiei stabilite de
	 * parcurgerea in latime, anterioara
	 */

	public static ArrayList<Location> topoSort(GameMap gameMap, int myID) {

		stack = new ArrayList<Location>();
		visited = new HashMap<Location, Boolean>();

		parent = new HashMap<Location, Direction>();

		for (int x = 0; x < gameMap.width; x++) {
			for (int y = 0; y < gameMap.height; y++) {
				final Location location = gameMap.getLocation(x, y);
				final Site site = location.getSite();

				if (site.owner == myID) {
					Direction dir = findDirection(location, gameMap);
					parent.put(location, dir);
				}
			}
		}

		for (Location location : parent.keySet()) {
			if (location.getSite().owner == myID)
				visited.put(location, false);
		}

		for (Location location : visited.keySet()) {
			if (visited.get(location) == false) {
				dfs(location, gameMap);
			}
		}

		Collections.reverse(stack);

		sortedIndex = new HashMap<Location, Integer>();

		for (int i = 0; i < stack.size(); i++) {
			sortedIndex.put(stack.get(i), i);
		}

		return stack;
	}

	/*
	 * Functia stabileste daca o celula din interior a adunat suficienta putere,
	 * caz in care apeleaza functia findDirection
	 */

	private static Direction whereToGo(Location currentLocation, GameMap gameMap) {

		if (currentLocation.getSite().strength < currentLocation.getSite().production * 4)
			return Direction.STILL;
		return findDirection(currentLocation, gameMap);
	}

	/*
	 * Metoda stabileste daca o celula e inconjurata doar de celule cu acelasi
	 * id.
	 */

	public static boolean isSurroundedByFriendlyCells(GameMap gameMap,
			Location location, int myID) {
		return ((gameMap.getLocation(location, Direction.EAST).getSite().owner == myID)
				&& (gameMap.getLocation(location, Direction.WEST).getSite().owner == myID)
				&& (gameMap.getLocation(location, Direction.NORTH).getSite().owner == myID) && (gameMap
				.getLocation(location, Direction.SOUTH).getSite().owner == myID));
	}

	/*
	 * Pentru celulele de pe marginea suprafetei noastre determina cea mai
	 * valoroasa celula vecina nedetinuta de noi, bazandu-se pe raportul
	 * production/strength pentru celulele care nu au in vecinatate un inamic si
	 * pe damage-ul pe care il da mutareaa pentru celulele inamice.
	 */

	private static Direction findBestToConquer(GameMap gameMap,
			Location currentLocation) {
		double raport = Integer.MIN_VALUE;
		int prod = Integer.MIN_VALUE;
		Direction minDirection = Direction.STILL;

		for (Direction direction : Direction.values()) {
			Location aux = gameMap.getLocation(currentLocation, direction);
			double efficiency;

			if (aux.getSite().owner == 0) {
				if (aux.getSite().strength == 0) {
					efficiency = aux.getSite().production;
				} else {
					efficiency = (double) aux.getSite().production
							/ (double) aux.getSite().strength;
				}

				if (efficiency > raport
						|| (efficiency == raport && aux.getSite().production > prod)) {
					minDirection = direction;
					raport = efficiency;
					prod = aux.getSite().production;
				}

				if (aux.getSite().strength < currentLocation.getSite().strength) {
					int damage = 0;
					int crtStrength = currentLocation.getSite().strength
							- aux.getSite().strength;
					for (Direction dir : Direction.values()) {
						Location neighbour = gameMap.getLocation(aux, dir);

						if (neighbour.getSite().owner != 0
								&& neighbour.getSite().owner != currentLocation
										.getSite().owner) {
							damage += crtStrength;
						}
					}

					if (damage >= raport) {
						raport = damage;
						minDirection = direction;
					}
				}
			}
		}

		return minDirection;
	}

	/*
	 * Functia stabileste daca o celula de pe marginea suprafetei detinute,
	 * ajutata de o celula vecina care nu poate cuceri nimic, poate sa
	 * cucereasca ea o celula din jur, caz in care va determina cea mai buna
	 * celula pe care poate sa o cucereasca si va returna "valoarea" celulei
	 * respective. "Valoarea" returnata e influentata de faptul ca acea celula
	 * are sau nu in jur celule inamice.
	 */

	private static double canConquer(GameMap gameMap, Location location,
			int previousStrength, int myID) {

		if (location.getSite().owner != myID
				|| isSurroundedByFriendlyCells(gameMap, location, myID))
			return 0;

		Direction directionNeighbour = findBestToConquer(gameMap, location);

		if (gameMap.getLocation(location, directionNeighbour).getSite().strength < location
				.getSite().strength) {
			return 0;
		}

		if (gameMap.getLocation(location, directionNeighbour).getSite().strength < location
				.getSite().strength + previousStrength) {

			Location next = gameMap.getLocation(location, directionNeighbour);
			float rapport;

			if (next.getSite().strength == 0) {
				rapport = (float) next.getSite().production;
			} else {
				rapport = (float) next.getSite().production
						/ next.getSite().strength;
			}

			int damage = 0;

			for (Direction dir : Direction.values()) {
				Location neighbour = gameMap.getLocation(next, dir);

				if (neighbour.getSite().owner != 0
						&& neighbour.getSite().owner != location.getSite().owner) {
					damage += neighbour.getSite().strength;
				}
			}

			if (damage >= rapport) {
				rapport = damage;
			}

			return rapport;
		}

		return 0;
	}

	/*
	 * Functia analizeaza tipul celulei detinute de noi si returneaza mutarea pe
	 * care aceasta trebuie sa o faca. In primul rand, face o distinctie intre
	 * celulele din interior si cele din exterior. Pentru cele din interior
	 * apeleaza functiile descrise mai sus. Pentru cele din exterior cauta cea
	 * mai valoroasa celula pe care o poate cuceri si de asemenea, inainte de
	 * stabilirea mutarii se intreaba daca a adunat suficienta putere. Daca nu
	 * poate sa cucereasca nicio alta casuta si totusi a adunat suficienta
	 * putere, verifica daca nu cumva poate ajuta un vecin sa cucereasca el o
	 * celula. Altfel , mai asteapta o tura.
	 */

	static Move moveToMake(GameMap gameMap, int x, int y, int myID) {
		Location currentLocation = gameMap.getLocation(x, y);
		Direction direction;

		if (isSurroundedByFriendlyCells(gameMap, currentLocation, myID)) {
			return new Move(currentLocation,
					whereToGo(currentLocation, gameMap));
		}

		direction = findBestToConquer(gameMap, currentLocation);

		if (gameMap.getLocation(currentLocation, direction).getSite().strength < currentLocation
				.getSite().strength) {

			Location next = gameMap.getLocation(currentLocation, direction);

			if (currentLocation.getSite().strength < currentLocation.getSite().production * 2) {
				return new Move(currentLocation, Direction.STILL);
			}
			return new Move(currentLocation, direction);
		}

		if (currentLocation.getSite().strength < currentLocation.getSite().production * 3) {
			return new Move(currentLocation, Direction.STILL);
		}

		double maxRapport = Double.MIN_VALUE;

		direction = Direction.STILL;

		for (Direction dir : Direction.values()) {
			Location next = gameMap.getLocation(currentLocation, dir);

			double rapport = canConquer(gameMap, next,
					currentLocation.getSite().strength, myID);

			if (rapport != 0) {
				if (rapport > maxRapport) {
					maxRapport = rapport;
					direction = dir;
				}
			}
		}

		return new Move(currentLocation, direction);
	}

	/*
	 * Calculeaza cea mai apropiata celula inamica de o celula detinuta de noi,
	 * pe linie sau pe coloana.
	 */
	static Pair closestEnemy(GameMap gameMap, Location location, int myID) {
		Pair enemy = new Pair();

		int line = location.getX();
		int column = location.getY();

		for (int i = 1; i < gameMap.width; i++) {
			final Location current = gameMap.getLocation(
					(line - i + gameMap.width) % gameMap.width, column);
			final Site site = current.getSite();

			if (site.owner > 0 && site.owner != myID) {
				enemy.setDirection(Direction.WEST);
				enemy.setDistance(i);
				break;
			}
		}

		for (int i = 1; i < gameMap.width; i++) {
			final Location current = gameMap.getLocation((line + i)
					% gameMap.width, column);
			final Site site = current.getSite();

			if (site.owner > 0 && site.owner != myID) {
				if (i < enemy.getDistance()) {
					enemy.setDirection(Direction.EAST);
					enemy.setDistance(i);
				}
				break;
			}
		}

		for (int j = 1; j < gameMap.height; j++) {
			final Location current = gameMap.getLocation(line,
					(column - j + gameMap.height) % gameMap.height);
			final Site site = current.getSite();

			if (site.owner > 0 && site.owner != myID) {
				if (j < enemy.getDistance()) {
					enemy.setDirection(Direction.NORTH);
					enemy.setDistance(j);
				}
				break;
			}
		}

		for (int j = 1; j < gameMap.height; j++) {
			final Location current = gameMap.getLocation(line, (column + j)
					% gameMap.height);
			final Site site = current.getSite();

			if (site.owner > 0 && site.owner != myID) {
				if (j < enemy.getDistance()) {
					enemy.setDirection(Direction.SOUTH);
					enemy.setDistance(j);
				}
				break;
			}
		}

		return enemy;
	}

	/*
	 * Calculeaza cea mai apropiata celula neutra de o celula detinuta de noi,
	 * pe linie sau pe coloana.
	 */
	static Pair closestNeutral(GameMap gameMap, Location location, int myID) {
		Pair enemy = new Pair();

		int line = location.getX();
		int column = location.getY();
		double raport = -1;

		for (int i = 1; i < gameMap.width; i++) {
			final Location current = gameMap.getLocation(
					(line - i + gameMap.width) % gameMap.width, column);
			final Site site = current.getSite();

			if (site.owner == 0) {
				enemy.setDirection(Direction.WEST);
				enemy.setDistance(i);
				if (site.strength == 0) {
					raport = site.production;
				} else {
					raport = ((double) site.production) / site.strength;
				}
				break;
			}
		}

		for (int i = 1; i < gameMap.width; i++) {
			final Location current = gameMap.getLocation((line + i)
					% gameMap.width, column);
			final Site site = current.getSite();

			if (site.owner == 0) {
				if (i < enemy.getDistance()) {
					enemy.setDirection(Direction.EAST);
					enemy.setDistance(i);
				}
				if (i == enemy.getDistance()) {
					if (site.strength == 0 && site.production > raport) {
						enemy.setDirection(Direction.EAST);
						enemy.setDistance(i);
						raport = site.production;
					} else if (site.strength != 0
							&& (double) site.production / site.strength > raport) {
						enemy.setDirection(Direction.EAST);
						enemy.setDistance(i);
						raport = ((double) site.production) / site.strength;
					}
				}
				break;
			}
		}

		for (int j = 1; j < gameMap.height; j++) {
			final Location current = gameMap.getLocation(line,
					(column - j + gameMap.height) % gameMap.height);
			final Site site = current.getSite();

			if (site.owner == 0) {
				if (j < enemy.getDistance()) {
					enemy.setDirection(Direction.NORTH);
					enemy.setDistance(j);
				}
				if (j == enemy.getDistance()) {
					if (site.strength == 0 && site.production > raport) {
						enemy.setDirection(Direction.NORTH);
						enemy.setDistance(j);
						raport = site.production;
					} else if (site.strength != 0
							&& (double) site.production / site.strength > raport) {
						enemy.setDirection(Direction.NORTH);
						enemy.setDistance(j);
						raport = ((double) site.production) / site.strength;
					}
				}
				break;
			}
		}

		for (int j = 1; j < gameMap.height; j++) {
			final Location current = gameMap.getLocation(line, (column + j)
					% gameMap.height);
			final Site site = current.getSite();

			if (site.owner == 0) {
				if (j < enemy.getDistance()) {
					enemy.setDirection(Direction.SOUTH);
					enemy.setDistance(j);
				}
				if (j == enemy.getDistance()) {
					if (site.strength == 0 && site.production > raport) {
						enemy.setDirection(Direction.SOUTH);
						enemy.setDistance(j);
						raport = site.production;
					} else if (site.strength != 0
							&& (double) site.production / site.strength > raport) {
						enemy.setDirection(Direction.SOUTH);
						enemy.setDistance(j);
						raport = ((double) site.production) / site.strength;
					}
				}
				break;
			}
		}

		return enemy;
	}
}
