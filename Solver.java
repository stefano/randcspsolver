/*
 * Random binary CSP optimization problem generator & solver
 * 
 * Authors:
 *  - Dissegna Stefano
 *  - Geremia Mirco
 */

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/*
 * ListDomain represents an explicit domain
 * Elements of the domain are integer values
 */
class ListDomain {

    private List<Integer> elems;

    public ListDomain(List<Integer> l) {
        elems = l;
    }

    /*
     *  create a singleton domain
     */
    public ListDomain(int val) {
        elems = new ArrayList<Integer>();
        elems.add(val);
    }

    /*
     *  @return list of elements in the domain
     */
    public List<Integer> getElems() {
        return elems;
    }

    /*
     * @return the min value of the domain
     */
    public int getMin() {
        int min = Integer.MAX_VALUE;
        for (int elem : elems) {
            if (elem < min) {
                min = elem;
            }
        }
        return min;
    }

    /*
     * @return the max value of the domain
     */
    public int getMax() {
        int max = Integer.MIN_VALUE;
        for (int elem : elems) {
            if (elem > max) {
                max = elem;
            }
        }
        return max;
    }


    /*
     * Remove from the domain the maximum value
     */
    public void removeMax() {
        Integer max = this.getMax();
        elems.remove(max);
    }


    /*
     * Remove inconsistent values from this domain
     * according to arc consistency between this, d2 and c
     * @return true if domain changed
     */
    public boolean removeInconsistent(ListDomain d2, BinaryConstraint c) {
        int n = elems.size();
        Iterator<Integer> i = elems.iterator();
        while (i.hasNext()) {
            int n1 = i.next();
            boolean found = false;
            for (int n2 : d2.elems) {
                if (c.satisfied(n1, n2)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                i.remove();
            }
        }
        return n != elems.size();
    }

    public ListDomain copy() {
        List<Integer> l = new ArrayList<Integer>(elems);
        return new ListDomain(l);
    }

    public boolean empty() {
        return elems.isEmpty();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("{ ");
        for (Integer i : elems) {
            sb.append(i.toString());
            sb.append(" ");
        }
        sb.append("}");

        return sb.toString();
    }
}

/*
 * A problem variable
 * has a name and a domain
 */
class Variable {

    private String name;
    private ListDomain domain;

    public Variable(String nam, ListDomain dom) {
        name = nam;
        domain = dom;
    }

    public ListDomain getDomain() {
        return domain;
    }

    public void setDomain(ListDomain n_dom) {
        domain.getElems().clear();
        domain = n_dom;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " = " + domain.toString();
    }

	public void toMinion(StringBuffer sb) {
		// output as a discrete variable
		// values ranges from minimum to maximum
		// domain values (conservative)
		sb.append("DISCRETE " + getName() + " {" + 
				domain.getMin() + ".." + domain.getMax() + "}\n");
	}

	/*
	 * Make a string representation of a list of variables
	 * suitable for minion
	 */
	static public String minionVarList(List<Variable> vars) {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		int n = 0;
		for (Variable v : vars) {
			sb.append(v.getName());
			if (n != vars.size()-1) {
				sb.append(",");
			}
			n++;
		}
		sb.append("]");
		return sb.toString();
	}
	
}

/*
 * A pair of integers
 */
class Pair {

    public Pair(int x1, int y1) {
        x = x1;
        y = y1;
    }
    public int x;
    public int y;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Pair other = (Pair) obj;
        if (x != other.x) {
            return false;
        }
        if (y != other.y) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "(" + x + "," + y + ")";
    }
}

/*
 * Constraint between two variable
 * explicit representation through pairs
 * of accepted values (integers)
 */
class BinaryConstraint {

    private HashSet<Pair> pairs;
    private Variable a;
    private Variable b;

    public BinaryConstraint(Variable a, Variable b) {
        pairs = new HashSet<Pair>();
        this.a = a;
        this.b = b;
    }

    public Variable getA() {
        return this.a;
    }

    public Variable getB() {
        return this.b;
    }

    public void add(Pair p) {
        pairs.add(p);
    }

    /*
     * Make a new binary constraint
     * which is a transposed version
     * of this one
     */
    public BinaryConstraint transpose() {
        BinaryConstraint bc = new BinaryConstraint(b, a);
        for (Pair p : pairs) {
        	// add each pair reversed
            bc.add(new Pair(p.y, p.x));
        }
        return bc;
    }

    /*
     * Tell if the pair (x, y) satisfies this constraint
     */
    public boolean satisfied(int x, int y) {
        return pairs.contains(new Pair(x, y));
    }

    /*
     * Remove from the domain of the first variable
     * values that don't have a support in the domain
     * of the second variable
     */
    public boolean revise() {
        return a.getDomain().removeInconsistent(b.getDomain(), this);
    }

    public String toString() {
        return "#<constraint(" + a.getName() + "," + b.getName() + "):" + pairs.toString() + ">";
    }

    /*
     * @return name of a minion table associated with this constraint
     */
    private String minionTable() {
    	return a.getName() + "_" + b.getName();
    }
    
	public void toMinion(StringBuffer sb) {
		sb.append(minionTable() + " " + pairs.size() + " 2\n");
		for (Pair p : pairs) {
			sb.append(p.x + " " + p.y + "\n");
		}
	}

	public void toMinionTable(StringBuffer sb) {
		sb.append("table([");
		sb.append(a.getName() + "," + b.getName() + "],");
		sb.append(minionTable() + ")\n");
	}
}

/*
 * A CSP optimization problem
 */
class Problem {

    public interface Evaluator {
        int eval(List<Variable> vars);
		void toMinion(StringBuffer sb, List<Variable> vars);
		void toMinionVariable(StringBuffer sb, List<Variable> vars);
		String minionName();
    }
    
    private List<Variable> vars;
    private List<BinaryConstraint> constraints;
    private List<BinaryConstraint> constraints_t; // transposed constraints
    private Evaluator heuristic;
    private Evaluator objectiveFunction;
    private List<Integer> sol;
    private int bound;
    private boolean propagation;
    private int visitedNodes; // track number of nodes visited by bb()

    public Problem(Evaluator h, Evaluator of, boolean prop) {
        heuristic = h;
        objectiveFunction = of;
        constraints = new ArrayList<BinaryConstraint>();
        constraints_t = new ArrayList<BinaryConstraint>();
        sol = new ArrayList<Integer>();
        bound = Integer.MIN_VALUE;
        propagation = prop;
        visitedNodes = 0;
    }

    public void setVariables(List<Variable> vars) {
        this.vars = vars;
    }

    private int evalHeuristic() {
        return heuristic.eval(vars);
    }

    private int evalObjectiveFunction() {
        return objectiveFunction.eval(vars);
    }

    private boolean notFailed() {
        for (Variable v : vars) {
            if (v.getDomain().empty()) {
                return false;
            }
        }
        return true;
    }

    private int getBound() {
        return this.bound;
    }

    private void setBound(int new_b) {
        this.bound = new_b;
    }

    public void addConstraint(BinaryConstraint bc) {
        constraints.add(bc);
        constraints_t.add(bc.transpose());
    }

    public boolean doPropagation() {
        return this.propagation;
    }

    /*
     * The AC-1 propagation algorithm
     */
    public void ac1() {
        boolean changed = true;
        while (changed) {
            changed = false;
            Iterator<BinaryConstraint> ic = constraints.iterator();
            Iterator<BinaryConstraint> ic_t = constraints_t.iterator();
            while (ic.hasNext()) {
                boolean a = ic.next().revise();
                boolean b = ic_t.next().revise();
                if (a || b) {
                    changed = true;
                }
            }
        }
    }

    /*
     * Return true if the current solution is a valid one
     */
    public boolean validSol() {
        int elem1, elem2;

        // for each constraint, check if it is satisfied
        for (BinaryConstraint bc : constraints) {
        	// variables are assumed to be assigned
        	// getMax() will return the only element
        	// in their domain
            elem1 = bc.getA().getDomain().getMax();
            elem2 = bc.getB().getDomain().getMax();
            if (bc.satisfied(elem1, elem2) == false) {
                return false;
            }
        }
     
        return true;
    }

    /*
     * Save the current solution as the best one
     */
    public void setSol() {
        sol.clear();
        for (Variable v : vars) {
            sol.add(v.getDomain().getElems().get(0)); // in a solution, every domain is a singleton
        }
    }

    public void printSol() {
        if (sol.isEmpty()) {
            System.out.println("No solutions.");
        }
        else {
            System.out.print("Solution: ");
            for (int s : sol) {
                System.out.print(s);
                System.out.print(' ');
            }
            System.out.println();
        }
    }

    public boolean hasSolution() {
    	return !sol.isEmpty();
    }

    public int getVisitedNodes() {
    	return visitedNodes;
    }
    
    /*
     * Branch&Bound implementation
     */
    public void bb(int lev) {
        Variable cv = this.vars.get(lev);
        List<ListDomain> dom_copy = new ArrayList<ListDomain>();

        ListDomain dom_tmp = cv.getDomain().copy(); // copy current domain
       
        while (dom_tmp.empty() == false) {
            dom_copy.clear();
            for (Variable v : vars) {
                dom_copy.add(v.getDomain().copy()); // copy all domains
            }
            ListDomain sing_dom = new ListDomain(dom_tmp.getMax());
            cv.setDomain(sing_dom);
            visitedNodes++; // node visited
            
            if (doPropagation()) {
                ac1();
            }
            // check if the propagation returned a failed CSP
            if (!doPropagation() || notFailed()) {
            	if ((lev + 1) < vars.size()) { // if it's not the last level
            		int h = evalHeuristic(); // heuristic on actual configuration of domains
            		if (h > getBound()) {
            			bb(lev + 1); // next level
            		}
            	} else { // last level
            		int of = evalObjectiveFunction();
            		if (of > getBound()) {
            			if (doPropagation() || validSol()) { // if propagation or, if not, if valid
            				setBound(of);
            				setSol(); // save current solution as the max values in domains
            			}
            		}
            	}
            }

            dom_tmp.removeMax();
            for (int i = 0; i < vars.size(); i++) { // restore domains
                vars.get(i).setDomain(dom_copy.get(i));
            }
        } // end while
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Solution: " + sol.toString());
        sb.append("#<Problem variables: ");
        for (Variable v : vars) {
            sb.append(v.toString());
            sb.append(", ");
        }
        sb.append("\nconstraints: ");

        for (BinaryConstraint bc : constraints) {
            sb.append(bc.toString());
            sb.append("\n");
        }
        sb.append(">");

        return sb.toString();
    }
    
    // output an equivalent minion file to sb
    public void toMinion(StringBuffer sb) {
    	// header
    	sb.append("MINION 3\n\n");
    	sb.append("**VARIABLES**\n\n");
    	// variables
    	for (Variable v : vars) {
    		v.toMinion(sb);
    	}
    	// variable to maximize
    	objectiveFunction.toMinionVariable(sb, vars);
    	// search
    	sb.append("**SEARCH**\n\n");
    	sb.append("MAXIMISING " + objectiveFunction.minionName() + "\n");
    	sb.append("PRINT [");

    	// variables output order
    	String varList = Variable.minionVarList(vars);
    	sb.append(varList);
    	sb.append("]\n");
    	sb.append("VARORDER ");
    	sb.append(varList);
    	sb.append("\n\n");
    	// constraints
    	sb.append("**TUPLELIST**\n");
    	for (BinaryConstraint c : constraints) {
    		c.toMinion(sb);
    	}
    	sb.append("**CONSTRAINTS**\n");
    	objectiveFunction.toMinion(sb, vars);
    	for (BinaryConstraint c : constraints) {
    		c.toMinionTable(sb);
    	}
    	// end
    	sb.append("**EOF**");
    }
}

/*
 * A randomly generated problem
 */
class RandomProblem extends Problem {

    private Random r = new Random();

    public RandomProblem(int nvars, int length, float density,
            float strictness, Evaluator h, Evaluator of, boolean prop) {
        super(h, of, prop);
        // create base domain that will be copied
        List<Integer> values = new ArrayList<Integer>();
        for (int i = 0; i < length; ++i) {
            values.add(i);
        }
        ListDomain dom = new ListDomain(values);
        List<Variable> vars = new ArrayList<Variable>();
        // create variables
        for (int i = 0; i < nvars; ++i) {
            vars.add(new Variable("V" + i, dom.copy()));
        }
        setVariables(vars);
        for (int i = 0; i < vars.size(); ++i) {
            for (int j = i + 1; j < vars.size(); ++j) {
            	// accept constraint with "density" probability
                if (r.nextFloat() <= density) {
                    // create constraint between v1 and v2
                    BinaryConstraint bc = new BinaryConstraint(vars.get(i), vars.get(j));
                    for (int a : dom.getElems()) {
                        for (int b : dom.getElems()) {
                        	// accept pair with "strictness" probability
                            if (r.nextFloat() <= strictness) {
                                bc.add(new Pair(a, b));
                            }
                        }
                    }
                    addConstraint(bc);
                }
            }
        }
    }
}

/*
 * Maximize sum of values of variables
 * good for both heuristic & objective function
 */
class MaxSum implements Problem.Evaluator {

    @Override
    public int eval(List<Variable> vars) {
        int sum = 0;
        for (Variable v : vars) {
            ListDomain d = v.getDomain();
            int max = Integer.MIN_VALUE;
            for (int elem : d.getElems()) {
                if (elem > max) {
                    max = elem;
                }
            }
            sum += max;
        }

        return sum;
    }

	@Override
	public String minionName() {
		return "SUM";
	}

	@Override
	public void toMinion(StringBuffer sb, List<Variable> vars) {
		String varList = Variable.minionVarList(vars);
		// couldn't find a sumeq() function
    	sb.append("sumleq(" + varList + "," + minionName() + ")\n");
    	sb.append("sumgeq(" + varList + "," + minionName() + ")\n");
	}
	
	/*
	 * Output to sb the declaration of the variable that will be maximized
	 */
	@Override
	public void toMinionVariable(StringBuffer sb, List<Variable> vars) {
		// use eval() for upper bound of variable domain
		// can't use Integer.MAX_VALUE since it would make
		// minion crash
		sb.append("DISCRETE SUM {0.." + eval(vars) + "}\n");		
	}
}

class Benchmark {
	public interface SingleRun {
	    public void setup();
		public void run();
		public Problem getProblem();
	}
	
	private SingleRun toRun;
	private int nrun;
	
	public Benchmark(SingleRun r, int nrun) {
		toRun = r;
		this.nrun = nrun;
	}
	
	public void runAll() {
		long max = Long.MIN_VALUE;
		long min = Long.MAX_VALUE;
		long total_time = 0;
		int visitedNodes = 0;
		int maxNodes = Integer.MIN_VALUE;
		int minNodes = Integer.MAX_VALUE;
		
		for (int i = 0; i < nrun; ++i) {
		    toRun.setup();
			long start = System.currentTimeMillis();
			toRun.run();
			long time = System.currentTimeMillis() - start;
			max = Math.max(max, time);
			min = Math.min(min, time);
			total_time += time;
			int nodes = toRun.getProblem().getVisitedNodes();
			maxNodes = Math.max(maxNodes, nodes);
			minNodes = Math.min(minNodes, nodes);
			visitedNodes += nodes;
		}
		
		// remove max & min value from the averages
		double avg = (total_time - (max + min)) / (nrun - 2.0);
		float avgNodes = (visitedNodes - (maxNodes + minNodes)) / (nrun - 2.0f);

		// output stats in CSV format
        System.out.print(";\"Max visited\";" + maxNodes);
        System.out.print(";\"Min visited\";" + minNodes);
        System.out.print(";\"Avg visited\";" + String.format("%f", avgNodes));
        System.out.print(";\"Max time\";" + max);
        System.out.print(";\"Min time\";" + min);        
        System.out.print(";\"Avg time\";" + String.format("%f", avg));
        System.out.println();
	}
}

class RandomProblemBenchmark implements Benchmark.SingleRun {	
	private int n;
	private int l;
	private float d;
	private float s;
	private boolean ac;
    private Problem p;
    private int solutions;
    
	public RandomProblemBenchmark(int n, int l, float d, float s, boolean ac) {
		this.n = n;
		this.l = l;
		this.d = d;
		this.s = s;
		this.ac = ac;
		solutions = 0;
	}
	
    public void setup() {
    	p = new RandomProblem(n, l, d, s, new MaxSum(), new MaxSum(), ac);
    }

	public void run() {
		p.bb(0);
		if (p.hasSolution()) {
			solutions++;
		}
	}
	
	public Problem getProblem() {
		return p;
	}

	/*
	 * Print benchmark parameters (the random problems class)
	 */
	public void printParameters() {
        System.out.print(";\"Num\";" + n);
        System.out.print(";\"Len\";" + l);
        System.out.print(";\"Den\";" + String.format("%f", d));
        System.out.print(";\"Str\";" + String.format("%f", s));
        System.out.print(";\"Prop\";" + ac);
	}

	// never used, print number of generated problem with a solution
	public void printStats() {
		System.out.println("Problems with solution: " + solutions);
	}
}

/*
 * Main class
 */
public class Solver {

    public static void main(String args[]) {
        int n = 3, l = 3;
        float d = 0.5f, s = 0.5f;
        boolean ac = false;
        boolean benchmark = false;
        int nrun = 0;
        boolean printMinion = false;
        String minionFileName = null;
        
        // command line parser - rudimental (no error checking)
        for (int i=0; i<args.length; i++) {
        	if (args[i].equals("-m")) {
        		printMinion = true;
        		minionFileName = args[i+1];
        		i++;
        	} 
        	else if (args[i].equals("-b")) {
        		benchmark = true;
                nrun = Integer.parseInt(args[i+1]);
                i++;
        	}
        	else if (args[i].equals("-n")) {
                n = Integer.parseInt(args[i+1]);
                i++;
            }
            else if(args[i].equals("-l")) {
                l = Integer.parseInt(args[i+1]);
                i++;
            }
            else if(args[i].equals("-d")) {
                d = Float.parseFloat(args[i+1]);
                i++;
            }
            else if(args[i].equals("-s")) {
                s = Float.parseFloat(args[i+1]);
                i++;
            }
            else if(args[i].equals("-ac")) {
                ac = true;
            } else {
                System.out.println("Error: unknown parameter.");
                System.out.println("Options:\t-n\t(int) number of variables");
                System.out.println("\t\t-l\t(int) cardinality of domains");
                System.out.println("\t\t-d\t(float) density of constraints");
                System.out.println("\t\t-s\t(float) strictness of constraints");
                System.out.println("\t\t-ac\tdo propagation");
                System.exit(1);
            }
        }

        if (benchmark) {
        	// benchmark mode
        	RandomProblemBenchmark rpb = new RandomProblemBenchmark(n, l, d, s, ac);
        	Benchmark b = new Benchmark(rpb, nrun);
        	rpb.printParameters();
        	b.runAll();
        } else {
        	// generate a problem and solve it
        	Problem p = new RandomProblem(n, l, d, s, new MaxSum(), 
        			new MaxSum(), ac);
        	if (printMinion) {
        		StringBuffer sb = new StringBuffer();
        		p.toMinion(sb);
        		try {
        			FileWriter f = new FileWriter(minionFileName);
        			f.write(sb.toString());
        			f.close();
        		} catch (IOException e) {
        			System.err.println("Error while creating file " + minionFileName);
        		}
        	}
        	System.out.println(p); // print generated problem
        	p.bb(0); // solve it
           	p.printSol(); // print its solution
        }
    }
}
