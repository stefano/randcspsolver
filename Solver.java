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

    public ListDomain(int val) {
    	elems = new ArrayList<Integer>();
    	elems.add(val);
    }

    public List<Integer> getElems() {
    	return elems;
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
    		for (int n2 :  d2.elems) {
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
    
    public ListDomain getDomain() { return domain; }

	public String getName() {
		return name;
	}
}

class Pair {
	public Pair(int x1, int y1) {
		x = x1;
		y = y1;
	}
	public int x;
	public int y;
}

/*
 * Constraint between two variable
 * explicit representation through pairs
 * of accepted values (integers)
 */
class BinaryConstraint extends HashSet<Pair>{

	private static final long serialVersionUID = 1L;

	private Variable a;
    private Variable b;

    public BinaryConstraint (Variable a, Variable b) {
    	super();
    	this.a = a;
    	this.b = b;
    }
    
    public BinaryConstraint transpose() {
    	BinaryConstraint bc = new BinaryConstraint(b, a);
    	for (Pair p : bc) {
    		bc.add(new Pair(p.y, p.x));
    	}
    	return bc;
    }

    public boolean satisfied(int x, int y) {
    	return contains(new Pair(x, y));
    }

    public boolean revise() {
    	return a.getDomain().removeInconsistent(b.getDomain(), this);
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
    }
}
