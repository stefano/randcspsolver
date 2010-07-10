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
            if (elem < min)
                min = elem;
        }
        return min;
    }

    /*
     * Return the minimal value of the domain
     */
    public int getMax() {
        int max = Integer.MIN_VALUE;
        for (int elem : elems) {
            if (elem > max)
                max = elem;
        }
        return max;
    }


    /*
     * Remove from the domain the minimal value
     */
    public void removeMin() {
        int min = this.getMin();
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
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair other = (Pair) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
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
}

/*
 * A CSP optimization problem
 */
class Problem {

    public interface Evaluator {
        int eval(List<Variable> vars);
    }
    
    private List<Variable> vars;
    private List<BinaryConstraint> constraints;
    private List<BinaryConstraint> constraints_t;
    private Evaluator heuristic;
    private Evaluator objectiveFunction;
    private int bound;
    private List<Integer> sol;

    public Problem(Evaluator h, Evaluator of) {
        heuristic = h;
        objectiveFunction = of;
        constraints = new ArrayList<BinaryConstraint>();
        constraints_t = new ArrayList<BinaryConstraint>();
        sol = new ArrayList<Integer>();
        bound = Integer.MIN_VALUE;
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

        Iterator<Variable> iv = this.vars.iterator();
        elem1 = iv.next().getDomain().getMax();
        while (iv.hasNext()) {
            elem2 = iv.next().getDomain().getMax();
            Iterator<BinaryConstraint> ic = this.constraints.iterator();
            while (ic.hasNext()) {
                if (ic.next().satisfied(elem1, elem2) == false) {
                    return false;
                }
            }
            elem1 = elem2;
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

    /*
     * Branch&Bound implementation
     */
    public void bb(int lev) { 
        Variable v = this.vars.get(lev);
        // warn: propagation may change values of ALL domains
        // not just current one
        ListDomain dom_cp = v.getDomain().copy(); // copy current domain
        // warn: during search, the variable should take
        // each value of the domain (as a singleton domain)
        // instead, each iteration a value is removed from the domain
        while (v.getDomain().empty() == false) {
        	// warn: if index out of bound, get doesn't
        	// return null, it throws an exception
            if (this.vars.get(lev+1) != null) { // if it's not the last level
            	// warn: current variable is not given a value
            	// heuristic becomes useless
            	// warn: missing propagation
            	int h = this.evalHeuristic(); // heuristic on actual configuration of domains
                if (h > this.getBound()) {
                    bb(lev+1); // next level
                } else { // Stop. Next try.
                	// warn: nothing changed, next iteration
                	// will be identical
                    continue;
                }
            } else { // last level
                int of = this.evalObjectiveFunction();
                if (this.validSol() && of > this.getBound()) {
                    this.setBound(of);
                    this.setSol(); // save current solution as the max values in domains
                }
            }
            v.getDomain().removeMin();
            // warn: all the domains should be restored now
            // before the next iteration
        }
        v.setDomain(dom_cp); // restore the domain
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
}

/*
 * A randomly generated problem
 */
class RandomProblem extends Problem {

    private Random r = new Random();

    public RandomProblem(int nvars, int length, float density,
            float strictness, Evaluator h, Evaluator of) {
        super(h, of);
        List<Integer> values = new ArrayList<Integer>();
        for (int i = 0; i < length; ++i) {
            values.add(i);
        }
        ListDomain dom = new ListDomain(values);
        List<Variable> vars = new ArrayList<Variable>();
        for (int i = 0; i < nvars; ++i) {
            vars.add(new Variable((new Integer(i)).toString(), dom.copy()));
        }
        setVariables(vars);
        for (int i = 0; i < vars.size(); ++i) {
            for (int j = i+1; j < vars.size(); ++j) {
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
}

public class Solver {

    public static void main(String args[]) {
        Problem p = new RandomProblem(3, 3, 0.5f, 0.5f,
                new MaxSum(), new MaxSum());
        p.bb(0);
        System.out.println(p);
    }
}
