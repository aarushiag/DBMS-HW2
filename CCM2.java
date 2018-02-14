package sem4;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;


class Passenger
{
	int id;
	ReentrantLock pass_lock =new ReentrantLock();
	int lock_mode=-1;
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
	ReentrantLock flight_lock =new ReentrantLock();
	int lock_mode=-1;
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
	
	
	void Reserve(int F, int id) throws InterruptedException
	{		
		if(db.hmap_flight.get(F).hmap_passengers.get(id)==null)
		{
				 while( !db.hmap_flight.get(F).flight_lock.tryLock() || db.hmap_flight.get(F).lock_mode==0)
				 {
					 Thread.sleep(1000);
				 }
				 
				 while(!db.hmap_passenger.get(id).pass_lock.tryLock() || db.hmap_passenger.get(id).lock_mode==0 )
				 {
					 Thread.sleep(1000);
				 }
				 
				 db.hmap_flight.get(F).lock_mode=1;
				 db.hmap_passenger.get(id).lock_mode=1;
				 
				 Flight flight=db.hmap_flight.get(F);
				 Passenger passenger=db.hmap_passenger.get(id);
				 
				 flight.hmap_passengers.put(id, passenger);
				 passenger.hmap_flights.put(F, flight);
				 
				 flight.num_res++;
				 System.out.println("Successfully reserved.");
				 
				 db.hmap_flight.get(F).flight_lock.unlock();
				 db.hmap_passenger.get(id).pass_lock.unlock();
				 
				 db.hmap_flight.get(F).lock_mode=-1;
				 db.hmap_passenger.get(id).lock_mode=-1;
		}
		
		else
		{
			System.out.println("This passenger has already booked this flight.");
		}
		
	}
	
	void Cancel(int F, int id) throws InterruptedException
	{
		 
		Flight flight=db.hmap_flight.get(F);
		Passenger passenger=db.hmap_passenger.get(id);
		if(flight.hmap_passengers.get(id)!=null)
		{	
			while( !db.hmap_flight.get(F).flight_lock.tryLock() || db.hmap_flight.get(F).lock_mode==0)
			 {
				 Thread.sleep(1000);
			 }
			 
			 while(!db.hmap_passenger.get(id).pass_lock.tryLock() || db.hmap_passenger.get(id).lock_mode==0 )
			 {
				 Thread.sleep(1000);
			 }
			
			db.hmap_flight.get(F).lock_mode=1;
			db.hmap_passenger.get(id).lock_mode=1;
			
			flight.hmap_passengers.remove(id);
			passenger.hmap_flights.remove(F);
			System.out.println("Successfully cancelled");
			flight.num_res--;
			
			db.hmap_flight.get(F).flight_lock.unlock();
			db.hmap_passenger.get(id).pass_lock.unlock();
			
			db.hmap_flight.get(F).lock_mode=-1;
			db.hmap_passenger.get(id).lock_mode=-1;
		}
		
		else
		{
			System.out.println("The passenger hasn't booked this flight");
		}
		
	}
	
	ArrayList<Flight> My_flight(int id) throws InterruptedException
	{ 
		 
		ArrayList<Flight> arr=new ArrayList<>();
		for(int i=0;i<db.flights.size();i++)
		{
			Flight flight =db.flights.get(i);
			while(flight.lock_mode==1)
			{
				
			}
			
			flight.lock_mode=0;			
			if(flight.hmap_passengers.get(id)!=null)
			{
				arr.add(flight);
			}
		}
		
		for(int i=0;i<db.flights.size();i++)
		{			
			Flight flight =db.flights.get(i);
			flight.lock_mode=-1;
		}
		
		
		return arr;
		
	}
	
	int Total_Reservation()
	{
		int sum=0;
		
		for(int u=0; u<db.flights.size(); u++)
		{
			Flight flight =db.flights.get(u);
			while(flight.lock_mode==1)
			{
				
			}
			
			flight.lock_mode=0;			
			
			sum+=flight.num_res;
		}
		

		for(int i=0;i<db.flights.size();i++)
		{			
			Flight flight =db.flights.get(i);
			flight.lock_mode=-1;
		}
		
		return sum;
	}
	
	void Transfer(int F1, int F2, int i) throws InterruptedException
	{
		Flight flight=db.hmap_flight.get(F1);
		Passenger passenger=db.hmap_passenger.get(i);
		
		Flight flight1=db.hmap_flight.get(F2);
		Passenger passenger1=db.hmap_passenger.get(i);
		
		if(flight.hmap_passengers.get(i)!=null)
		{
			if(F1<F2)
			{
				while(!flight.flight_lock.tryLock() || flight.lock_mode==0)
				{
					Thread.sleep(1000);
				}				
				
				flight.lock_mode=1;
				
				while(!flight1.flight_lock.tryLock() || flight1.lock_mode==0)
				{
					Thread.sleep(1000);
				}
				flight1.lock_mode=-1;
							
			}
			
			else
			{
				 
				while(!flight1.flight_lock.tryLock() || flight1.lock_mode==0)
				{
					Thread.sleep(1000);
				}
				
				flight1.lock_mode=-1;
				
				while(!flight.flight_lock.tryLock() || flight.lock_mode==0)
				{
					Thread.sleep(1000);
				}			
				
				flight.lock_mode=1;
				
			}
			
			flight.hmap_passengers.remove(i);
			passenger.hmap_flights.remove(F1);
			//System.out.println("Successfully cancelled");
			
			if(db.hmap_flight.get(F2).hmap_passengers.get(i)==null)
			{ 
				 flight1.hmap_passengers.put(i,passenger1);
				 passenger1.hmap_flights.put(F2, flight1);
				 flight1.num_res++;
			}
			
			System.out.println("Successfully transferred");
			if(F1<F2)
			{
				flight1.lock_mode=-1;
			}
			else
			{
				flight.lock_mode=-1;
			}
		}
		
		else
		{
			System.out.println("The passenger hasn't booked flight F1, transfer failed");
		}
		
	}

	@Override
	public void run() 
	{	
		
		 int cnt=0;
		
		do
		{			
			System.out.println(Thread.currentThread());
//			Select a transaction type randomly
//			Select object(flight and passenger id) for transaction randomly
//			Invoke transaction
			Random generator = new Random();
			int trans_type = generator.nextInt(5) + 1;
			
			if(trans_type == 1)	//reserve
			{
				System.out.println("RESERVE");
				int F = generator.nextInt(num_flights)+1;
				int id = generator.nextInt(num_passengers)+1;
				
				try {
					Reserve(F,id);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			else if(trans_type == 2)	//cancel
			{
				System.out.println("CANCEL");
				int F = generator.nextInt(num_flights)+1;
				int id = generator.nextInt(num_passengers)+1;
	
				try {
					Cancel(F,id);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			else if(trans_type == 3)	//my flights
			{
				System.out.println("MY_FLIGHTS");
				int x=generator.nextInt(num_passengers)+1;
				ArrayList<Flight> t;
				try {
					t = My_flight(x);
					for(int u=0; u<t.size(); u++)
					{
						System.out.print("Flight ids for reserved flights for passenger "+x+": ");
						System.out.print(t.get(u).F+" ");
					}
					System.out.println();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
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
			
				try {
					Transfer(F1, F2, id);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			cnt++;
			
		}while(cnt<5);
		
		
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

public class CCM2
{
	public static void main(String[] args) 
	{
		Database obj=new Database();
			
		Random generator =new Random();
		int num_passengers =10; //Generate a random number of passengers (5 to 10)
		int num_flights =5; //Generate a random number of passengers (3 to 5)
			
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
