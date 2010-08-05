
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

    // create a singleton domain
    // i.e. a domain with just one value
    public ListDomain(int val) {
        elems = new ArrayList<Integer>();
        elems.add(val);
    }

    public List<Integer> getElems() {
        return elems;
    }

    /*
     * Return the minimal value of the domain
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
     * Return the minimal value of the domain
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
     * Remove from the domain the minimal value
     */
    public void removeMin() {
        Integer min = this.getMin();
        elems.remove(min);
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
		sb.append("DISCRETE " + getName() + " {" + 
				domain.getMin() + ".." + domain.getMax() + "}\n");
	}
}

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

    public BinaryConstraint transpose() {
        BinaryConstraint bc = new BinaryConstraint(b, a);
        for (Pair p : pairs) {
            bc.add(new Pair(p.y, p.x));
        }
        return bc;
    }

    public boolean satisfied(int x, int y) {
        return pairs.contains(new Pair(x, y));
    }

    public boolean revise() {
        return a.getDomain().removeInconsistent(b.getDomain(), this);
    }

    public String toString() {
        return "#<constraint(" + a.getName() + "," + b.getName() + "):" + pairs.toString() + ">";
    }

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

		void toMinion(StringBuffer sb);

		String minionName();
    }
    private List<Variable> vars;
    private List<BinaryConstraint> constraints;
    private List<BinaryConstraint> constraints_t;
    private Evaluator heuristic;
    private Evaluator objectiveFunction;
    private List<Integer> sol;
    private int bound;
    private boolean propagation;

    public Problem(Evaluator h, Evaluator of, boolean prop) {
        heuristic = h;
        objectiveFunction = of;
        constraints = new ArrayList<BinaryConstraint>();
        constraints_t = new ArrayList<BinaryConstraint>();
        sol = new ArrayList<Integer>();
        bound = Integer.MIN_VALUE;
        propagation = prop;
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

        for (BinaryConstraint bc : constraints) {
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
            }
            System.out.println();
        }
    }

    /*
     * Branch&Bound implementation
     */
    public void bb(int lev) {
        Variable cv = this.vars.get(lev);
        List<ListDomain> dom_copy = new ArrayList<ListDomain>();

        if (this.doPropagation()) {
                this.ac1();
        }
        ListDomain dom_tmp = cv.getDomain().copy(); // copy current domain
       
        while (dom_tmp.empty() == false) {
            dom_copy.clear();
            for (Variable v : vars) {
                dom_copy.add(v.getDomain().copy()); // copy all domains
            }
            ListDomain sing_dom = new ListDomain(dom_tmp.getMin());
            cv.setDomain(sing_dom);
            
            if (this.doPropagation()) {
                this.ac1();
            }

            if ((lev + 1) < this.vars.size()) { // if it's not the last level
                int h = this.evalHeuristic(); // heuristic on actual configuration of domains
                if (h > this.getBound()) {
                    bb(lev + 1); // next level
                } 
            } else { // last level
                int of = this.evalObjectiveFunction();
                if (of > this.getBound()) {
                    if (this.doPropagation() || this.validSol()) { // if propagation or, if not, if valid
                        this.setBound(of);
                        this.setSol(); // save current solution as the max values in domains
                    }
                }
            }

            dom_tmp.removeMin();
            for (int i = lev; i < this.vars.size(); i++) { // restore next domains from the current level
                this.vars.get(i).setDomain(dom_copy.get(i));
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
    	objectiveFunction.toMinion(sb);
    	// search
    	sb.append("**SEARCH**\n\n");
    	sb.append("MAXIMISING " + objectiveFunction.minionName() + "\n");
    	sb.append("PRINT [");
    	StringBuffer sb2 = new StringBuffer();
    	sb2.append("[");
    	int n = 0;
    	for (Variable v : vars) {
    		sb2.append(v.getName());
    		if (n != vars.size()-1) {
    			sb2.append(",");
    		}
    		n++;
    	}
    	sb2.append("]");
    	String varList = sb2.toString();
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
    	sb.append("sumleq(" + varList + "," + objectiveFunction.minionName() + ")\n");
    	sb.append("sumgeq(" + varList + "," + objectiveFunction.minionName() + ")\n");
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
        List<Integer> values = new ArrayList<Integer>();
        for (int i = 0; i < length; ++i) {
            values.add(i);
        }
        ListDomain dom = new ListDomain(values);
        List<Variable> vars = new ArrayList<Variable>();
        for (int i = 0; i < nvars; ++i) {
            vars.add(new Variable("V" + i, dom.copy()));
        }
        setVariables(vars);
        for (int i = 0; i < vars.size(); ++i) {
            for (int j = i + 1; j < vars.size(); ++j) {
                if (r.nextFloat() <= density) {
                    // create constraint between v1 and v2
                    BinaryConstraint bc = new BinaryConstraint(vars.get(i), vars.get(j));
                    for (int a : dom.getElems()) {
                        for (int b : dom.getElems()) {
                            if (r.nextFloat() <= strictness) {
                                // accept pair
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
	public void toMinion(StringBuffer sb) {
		sb.append("DISCRETE SUM {" + Integer.MIN_VALUE + ".." + Integer.MAX_VALUE + "}\n");
	}
}

public class Solver {

    public static void main(String args[]) {
        int n = 3, l = 3;
        float d = 0.5f, s = 0.5f;
        boolean ac = false;
        boolean printMinion = false;

        // command line parser - rudimental [solo per provare]
        // TODO: error handling
        for (int i=0; i<args.length; i++) {
        	if (args[i].equals("-m")) {
        		printMinion = true;
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
            }
            else {
                System.out.println("Error: unknown parameter.");
            }
        }

        Problem p = new RandomProblem(n, l, d, s,
                new MaxSum(), new MaxSum(), ac);
        if (printMinion) {
        	StringBuffer sb = new StringBuffer();
        	p.toMinion(sb);
        	System.out.println(sb.toString());
        }
        System.out.println(p);
        p.bb(0);
        p.printSol();
    }
}
