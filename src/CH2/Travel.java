package CH2;
/**
* @author Eric_Chan
* @version 2016.10.05
*/
abstract class TravelSuper 
{
	String travelMethod;
	abstract void howToTravel();
}

class TravelCar extends TravelSuper
{
	public void howToTravel()
	{
		super.travelMethod = "开车去旅行";
	}
}

class TravelTrain extends TravelSuper
{
	public void howToTravel()
	{
		super.travelMethod = "搭火车去旅行";
	}
}

class TravelPlane extends TravelSuper
{
	public void howToTravel()
	{
		super.travelMethod = "搭飞机去旅行";
	}
}

/**
 * 简单工厂与策略模式的结合
 */
class TravelContext
{
	TravelSuper ts = null;
	TravelContext(String type)
	{
		switch(type)
		{
			case "plane": ts = new TravelPlane();break;
			case "train": ts = new TravelTrain();break;
			case "car": ts = new TravelCar();break;
		}
	}
	
	String getResult()
	{
		ts.howToTravel();
		return ts.travelMethod;
	}
}

public class Travel 
{
	public static void main(String args[])
	{
		String plan = "car";  // plane car
		TravelContext tc = new TravelContext(plan);
		System.out.println(tc.getResult());
	}

}
