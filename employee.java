// SANKALP AGRAWAL, 2017363

import java.util.*;

// I have assumed beforehand that the user will enter the correct employee id while 
// he is making his query and the correct skill level of the person(1,2 or 3).

// Multiple insurance options can also be selected.

public class employee {
	public int skill;			   // Skill level of employee
	public int[] insurances;		//  array of size 3 (insurances[i]=1 if option i is valid)
	public int hours;				// hours spent by the worker
	public int id;					// unique id of employee
	public static int counter=0;	// to increment the id	

	public employee() {		// default constructor
		this.skill=0;
		this.hours=0;
		this.insurances = new int[3];
		this.id=counter+1;
		counter++;
	}

	public employee(int skill,int hours,int[] insur) {		// paramterized constructor
		this.skill=skill;
		this.hours=hours;
		this.insurances = insur;
		this.id = counter+1;
		counter++;
	}

	public int getpayrate() {			// method to get payrate as per skill level
		if(this.skill==1) return 170;
		else if(this.skill==2) return 200;
		else if(this.skill==3) return 450;
		else return 0;
	}

	public int getregpay(int payrate) { 		// method to get regular pay(less than 40) in terms of payrate
		if(this.hours<40) return this.hours * payrate;
		else return 40 * payrate; 
	}

	public int getovertimepay(int payrate) {		// method to get overtime pay(for hours above 40)
		if(this.hours>40) return (this.hours-40) * (payrate)*(3/2);
		else return 0;
	}

	public int gettotalpay(int payrate) { 		// method to get total pay(regular+overtime)
		return this.getregpay(payrate) + this.getovertimepay(payrate);
	}

	public float getreducedpay() { 		// method to find deductions in cost as per insurance scheme
		float red = 0;
		if(this.insurances[0]==1) red += 32.5;
		if(this.insurances[1]==1) red += 20.0;
		if(this.insurances[2]==1) red += 10.0;
		return red;
	}

	public float getnetpay(int payrate) {		// method to get netpay(including deductions). If netpay<0, method will return -1.
		if(this.gettotalpay(payrate)>=this.getreducedpay()) {
			return this.gettotalpay(payrate) - this.getreducedpay();
		}
		else
			return -1;
	}

	public static boolean contains(String[] arr, String val) {		// method to find whether a string ("1","2" or "3") is included in the insurances array.
		for(int i=0;i<arr.length;i++) {
			if(val.equals(arr[i])) return true;
		}
		return false;
	}

	public static employee getemployee(employee arr[],int num) {	// method to get employee object corresponding to a particular id. 
		for(int i=0;i<arr.length;i++) {
			if(arr[i].id==num) return arr[i];
		}
		return null;
	}

	public static float companytotal(employee arr[]) {		// method to get the total gross pay for all the employees.
		float tot=0;
		for(int i=0;i<arr.length;i++) {
			int payt = arr[i].getpayrate();
			float rr = arr[i].getnetpay(payt);
			tot += rr;
		}
		return tot;
	}

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		System.out.print("Enter no. of employees:");
		int n = in.nextInt();
		employee[] ee = new employee[n]; 	// ee is the array of employee objects.
		for(int i=0;i<n;i++) {				// this loop will run n times, taking info for all the employees the user wants to enter.
			int[] insur = new int[3];		// insur array will be passed as insurances array to employee object.
			System.out.println("Enter information for employee" + (i+1) + "--");
			System.out.print("Enter skill level:");
			int ski = in.nextInt();
			System.out.print("Enter no. of hours worked:");
			int hrs = in.nextInt();
			System.out.println("Enter insurance options(seperated with a space):");
			String bakk = in.nextLine();
			String[] opts = new String[3];		// this array will be blank if skill==1, else it will contain the insurance optins opted by employees.
			if(ski==2||ski==3) {
				opts = in.nextLine().split(" ");
			}
			else {
				System.out.println("Not applicable");
			}
			if(contains(opts,"1")) {
				insur[0]=1;
			}
			if(contains(opts,"2")) {
				insur[1]=1;
			}
			if(contains(opts,"3")) {
				insur[2]=1;
			}
			ee[i] = new employee(ski,hrs,insur);	// new employee object is inserted at ith position of array
			System.out.println();
		}

		System.out.println();
		System.out.println("Menu:");		// Menu containing all options available for the user.
		System.out.println("1. Hours Worked");
		System.out.println("2. Hourly pay rate");
		System.out.println("3. Regular pay");
		System.out.println("4. Overtime pay");
		System.out.println("5. Total pay");
		System.out.println("6. Total itemized deductions");
		System.out.println("7. Net Pay the individual employee");
		System.out.println("8. Net Pay for the company");
		System.out.println("9. Change Employee id");
		System.out.println("10. Exit");
		System.out.println();
		System.out.println("Enter employee number for whom you want to search:");
		int empid = in.nextInt();
		employee curr = getemployee(ee,empid);
		while(true) {
			System.out.print("Select option:");
			int op = in.nextInt();
			int payy = curr.getpayrate();
			if(op==1) System.out.println("Hours worked:" + curr.hours);
			else if(op==2) System.out.println("Hourly pay rate:" + payy);
			else if(op==3) System.out.println("Regular pay:" + curr.getregpay(payy));
			else if(op==4) System.out.println("Overtime pay:" + curr.getovertimepay(payy));
			else if(op==5) System.out.println("Total pay:" + curr.gettotalpay(payy));
			else if(op==6) System.out.println("Total itemized deductions:" + curr.getreducedpay());
			else if(op==7) {
				float f = curr.getnetpay(payy);
				if(f==-1) { 	// if getnetpay() method returns -1, then the employee can't opt for insurance
					System.out.println("error, can't opt for insurance.");
				}
				else {
					System.out.println("Net pay:" + f);
				}
			}
			else if(op==8) {
				System.out.println("The net payment the company has to make for all employees:" + companytotal(ee));
			}
			else if(op==9) {	// it will change the employee number. ***Precondition-- the employee id will be a valid one.
				System.out.print("Enter employee number:");	
				int chan = in.nextInt();
				curr = getemployee(ee,chan);
				payy = curr.getpayrate();
			}
			else if(op==10) break;
			else {
				System.out.println("wrong entry");
				break;
			}
		}



	}
}