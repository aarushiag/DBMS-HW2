package sem4;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;


class Passenger
{
	int id;
	HashMap<Integer,Flight> hmap_flights = new HashMap<Integer,Flight>();
	
	
	public Passenger(int i)
	{
		id=i;
	}
	
	public void setflight(Flight flight)
	{
		hmap_flights.put(flight.F, flight);
	}
}

class Flight
{
	int F;
	int num_res;
	//ArrayList<Passenger> passengers;
	HashMap<Integer,Passenger> hmap_passengers = new HashMap<Integer,Passenger>();
	
	public Flight(int f)
	{
		F=f;
		num_res=0;
	}
}

class Transaction implements Runnable
{
	Database db;
	String type;
	static ReentrantLock lock =new ReentrantLock();
	int num_flights;
	int num_passengers;
	
	
	void Reserve(int F, int id)
	{
		if(db.hmap_flight.get(F).hmap_passengers.get(id)==null)
		{
				 Flight flight=db.hmap_flight.get(F);
				 Passenger passenger=db.hmap_passenger.get(id);
				 
				 flight.hmap_passengers.put(id, passenger);
				 passenger.hmap_flights.put(F, flight);
				 
				 flight.num_res++;
				 System.out.println("Successfully reserved.");
		}
		
		else
		{
			System.out.println("This passenger has already booked this flight.");
		}
		
	}
	
	void Cancel(int F, int id)
	{
		 
		Flight flight=db.hmap_flight.get(F);
		Passenger passenger=db.hmap_passenger.get(id);
		if(flight.hmap_passengers.get(id)!=null)
		{
			flight.hmap_passengers.remove(id);
			passenger.hmap_flights.remove(F);
			System.out.println("Successfully cancelled");
			flight.num_res--;
		}
		
		else
		{
			System.out.println("The passenger hasn't booked this flight");
		}
		
	}
	
	ArrayList<Flight> My_flight(int id)
	{ 
		ArrayList<Flight> arr=new ArrayList<>();
		for(int i=0;i<db.flights.size();i++)
		{
			Flight flight =db.flights.get(i);
			if(flight.hmap_passengers.get(id)!=null)
			{
				arr.add(flight);
			}
		}
		
		return arr;
		
	}
	
	int Total_Reservation()
	{
		int sum=0;
		
		for(int u=0; u<db.flights.size(); u++)
		{
			sum+=db.flights.get(u).num_res;
		}
		
		return sum;
	}
	
	void Transfer(int F1, int F2, int i)
	{
		Flight flight=db.hmap_flight.get(F1);
		Passenger passenger=db.hmap_passenger.get(i);
		if(flight.hmap_passengers.get(i)!=null)
		{
			flight.hmap_passengers.remove(i);
			passenger.hmap_flights.remove(F1);
			//System.out.println("Successfully cancelled");
			
			if(db.hmap_flight.get(F2).hmap_passengers.get(i)==null)
			{
					 Flight flight1=db.hmap_flight.get(F2);
					 Passenger passenger1=db.hmap_passenger.get(i);
					 
					 flight1.hmap_passengers.put(i,passenger1);
					 passenger1.hmap_flights.put(F2, flight1);
					 
					 flight1.num_res++;
			}
			
			System.out.println("Successfully transferred");
		}
		
		else
		{
			System.out.println("The passenger hasn't booked flight F1, transfer failed");
		}
		
	}

	@Override
	public void run() 
	{	
		
		while(!lock.tryLock())
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Random generator = new Random();
		int random = generator.nextInt(5)+1;
		int cnt=0;
		
		do
		{			
//			Select a transaction type randomly
//			Select object(flight and passenger id) for transaction randomly
//			Invoke transaction
			generator = new Random();
			int trans_type = generator.nextInt(5) + 1;
			
			if(trans_type == 1)	//reserve
			{
				System.out.println("RESERVE");
				int F = generator.nextInt(num_flights)+1;
				int id = generator.nextInt(num_passengers)+1;
				
				Reserve(F,id);
			}
			
			else if(trans_type == 2)	//cancel
			{
				System.out.println("CANCEL");
				int F = generator.nextInt(num_flights)+1;
				int id = generator.nextInt(num_passengers)+1;
				
				Cancel(F,id);
			}
			
			else if(trans_type == 3)	//my flights
			{
				System.out.println("MY_FLIGHTS");
				int x=generator.nextInt(num_passengers)+1;
				ArrayList<Flight> t = My_flight(x);
				
				for(int u=0; u<t.size(); u++)
				{
					System.out.print("Flight ids for reserved flights for passenger "+x+": ");
					System.out.print(t.get(u).F+" ");
				}
				System.out.println();
			}
			
			else if(trans_type == 4)	//total reservations
			{
				System.out.println("TOTAL RESERVATIONS");
				System.out.println(Total_Reservation());
			}
			
			else if(trans_type == 5)	//transfer
			{
				System.out.println("TRANSFER");
				int F1 = generator.nextInt(num_flights)+1;
				int F2 = generator.nextInt(num_flights)+1;
				int id = generator.nextInt(num_passengers)+1;
			
				Transfer(F1, F2, id);
			}
			
			cnt++;
			
		}while(cnt<random);
		
		lock.unlock();
		 
	}
}

class Database 
{
	volatile ArrayList<Flight> flights=new ArrayList<Flight>();
	volatile ArrayList<Passenger> passengers = new ArrayList<Passenger> ();
	volatile ArrayList<Transaction> transaction = new ArrayList<Transaction>();
	volatile HashMap<Integer, Flight> hmap_flight = new HashMap<Integer, Flight>();
	volatile HashMap<Integer, Passenger> hmap_passenger = new HashMap<Integer, Passenger> () ;
}

public class CCM
{
	public static void main(String[] args) 
	{
		Database obj=new Database();
			
		Random generator =new Random();
		int num_passengers = generator.nextInt(6)+5; //Generate a random number of passengers (5 to 10)
		int num_flights = generator.nextInt(3)+3; //Generate a random number of passengers (3 to 5)
			
		for(int j=0; j<num_flights; j++)
		{
			Flight f = new Flight(j+1);
			obj.flights.add(f);
			obj.hmap_flight.put(f.F,f);
		}
			
		for(int i=0; i<num_passengers; i++)
		{
			Passenger p = new Passenger(i+1);
			obj.passengers.add(p);
			obj.hmap_passenger.put(p.id, p);
				
			Flight f=obj.flights.get(i%num_flights);				 
			f.hmap_passengers.put(p.id,p);
			p.hmap_flights.put(f.F,f);
			f.num_res++;
		}
			

		int num_trans = 5;
		Transaction arr[]=new Transaction[num_trans];
		for(int i=0;i<num_trans;i++)
		{
			arr[i]=new Transaction();
			arr[i].db=obj;
			arr[i].num_flights = num_flights;
			arr[i].num_passengers = num_passengers;
		}
		
		
		Thread pool[]=new Thread[num_trans];
			
		for(int i=0;i<num_trans;i++)
		{
			pool[i]=new Thread(arr[i]);				  
		}
			
		boolean visited[]=new boolean[num_trans];
				
		for(int y=0; y<num_trans; y++)
		{
			pool[y].run();
		}
	}
}