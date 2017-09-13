public class Location implements Comparable {

	// Public for backward compability
	public final int x, y;
	private final Site site;
	private Location closest;

	public Location(int x, int y, Site site) {
		this.x = x;
		this.y = y;
		this.site = site;
		this.closest = null;
	}

	public Location(int x, int y, Site site, Location location) {
		this.x = x;
		this.y = y;
		this.site = site;
		this.closest = location;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public Site getSite() {
		return site;
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof Location) && (x == ((Location) o).x)
				&& (y == ((Location) o).y);
	}

	@Override
	public int hashCode() {
		return x + y;
	}

	@Override
	public int compareTo(Object o) {
		Location aux = (Location) o;
		float rap1, rap2;
		if(aux.getSite().strength == 0)
			rap1 = (float) aux.getSite().production;
		else
			rap1 = (float) aux.getSite().production / aux.getSite().strength;
		
		if(site.strength == 0)
			rap2 = (float) site.production;
		else
			rap2 = (float) site.production / site.strength;
		
		return Float.compare(rap1, rap2);
	}
}
