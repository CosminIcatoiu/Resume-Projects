import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyBot {

	public static void main(String[] args) throws java.io.IOException {

		final InitPackage iPackage = Networking.getInit();
		final int myID = iPackage.myID;
		final GameMap gameMap = iPackage.map;

		Networking.sendInit("MyJavaBot");

		int frame = 0;
		int frame2 = 0;

		while (true) {
			List<Move> moves = new ArrayList<Move>();

			Networking.updateFrame(gameMap);

            /* Este determinata distanta minima pana la celulele adverse. */
			MoveMaker.bfs(gameMap, myID, 1);

			/* Este determinata distanta minima pana la celulele neutre. */		
			MoveMaker.bfs(gameMap, myID, 0);

			/* Se determina ordinea in care mutarile trebuiesc efectuate. */
			ArrayList<Location> order = MoveMaker.topoSort(gameMap, myID);

			/* Este creata lista de mutari pentru frame-ul curent. */
			for (Location location : order) {
				moves.add(MoveMaker.moveToMake(gameMap, location.x, location.y,
						myID));
			}

			Networking.sendFrame(moves);
		}
	}
}
