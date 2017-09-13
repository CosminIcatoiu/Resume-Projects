
public class Pair {

	private int distance;
	private Direction direction;
	
	public Pair () {
		setDirection(Direction.STILL);
		setDistance(Integer.MAX_VALUE);
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}
}
