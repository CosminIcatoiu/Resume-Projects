
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public class LocationComparator implements Comparator<Location> {

	GameMap gameMap;
	
	public LocationComparator(GameMap gameMap){
		this.gameMap = gameMap;
	}
	
	@Override
	public int compare(Location l1, Location l2) {
		int sum1 = 0, sum2 = 0;
		
		for(Direction dir : Direction.values()) {
			if(gameMap.getLocation(l1, dir).getSite().owner != 0) {
				sum1 += gameMap.getLocation(l1, dir).getSite().strength;
			}
			if(gameMap.getLocation(l2, dir).getSite().owner != 0) {
				sum1 += gameMap.getLocation(l2, dir).getSite().strength;
			}
		}
		
		return sum2 - sum1;
	}
}
