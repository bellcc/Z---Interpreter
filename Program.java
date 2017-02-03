import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class Program
{
    private HashMap<String, Object> variables = new HashMap<String, Object>();
    private List<String> lines = new ArrayList<String>();
    private int currentLine;

    public Program()
    {
        this(new HashMap<String, Object>(), new ArrayList<String>(), new Integer(0));
    }

    public Program(ArrayList<String> lines)
    {
        this(new HashMap<String, Object>(), lines, new Integer(0));
    }

    public Program(HashMap<String, Object> variables, ArrayList<String> lines, int currentLine)
    {
        this.variables = variables;
        this.lines = lines;
        this.currentLine = 0;
    }

    public void interpret()
    {
        for(currentLine=0;currentLine<lines.size();currentLine++)
        {
            String parts[] = lines.get(currentLine).split(" ");

            if(parts[0].equals("PRINT"))
            {
                print(lines.get(currentLine).substring(6));
            }
            else if(parts[0].equals("FOR"))
            {
                loop(lines.get(currentLine));
            }
            else
            {
                String key = parts[0];
                String operator = parts[1];
                String expression = parts[2];

                assignment(key, operator, expression);
            }
        }
    }

    private void loop(String expression)
    {
        String loopExp = extractLoop(expression);
        parseLoop(loopExp);
    }
    
    private void executeLoop(int count, ArrayList<String> statements)
    {     	
    	for(int j=0;j<count;j++)
    	{
        	for(int i=0;i<statements.size();i++)
        	{    		
        		String statement = statements.get(i);
        		
        		if(statement.startsWith("PRINT"))
        		{
        			print(statement);
        		}
        		else if(statement.startsWith("FOR"))
        		{
        			parseLoop(statement);
        		}
        		else
        		{
        			String parts[] = statement.split(" ");
        			
                    String key = parts[0];
                    String operator = parts[1];
                    String expression = parts[2];

                    assignment(key, operator, expression);
        		}
        	}
    	}
    }

    private void parseLoop(String expression)
    {
        String parts[] = expression.split(";");

        int index = parts[0].indexOf(" ", 4);
        int count = Integer.parseInt(parts[0].substring(4, index));

        ArrayList<String> statements = new ArrayList<String>();
        statements.add(parts[0].substring(index, parts[0].length()).trim());
        
        for(int i=1;i<parts.length;i++)
        {
        	if(parts[i].trim().startsWith("ENDFOR"))
        	{
        		String statement = parts[i].replaceAll("ENDFOR", "").trim();
        		
        		if(statement.length() != 0)
        		{
            		statements.add(statement);
        		}
        		
        		continue;
        	}
        	else if(parts[i].trim().substring(0, 3).equals("FOR"))
        	{
        		String loop = extractLoop(expression.substring(expression.indexOf(parts[i].trim()), expression.length()));
        		statements.add(loop);
        	}
        	else
        	{        		
            	statements.add(parts[i].trim());
        	}
        }
        
        executeLoop(count, statements);
    }

    private String extractLoop(String expression)
    {
        String endTkn = "ENDFOR";
        String forTkn = "FOR";
        String newExp = "";

        int skip = 0;
        for(int i=0;i<=expression.length() - endTkn.length();i++)
        {
            String sub = expression.substring(i, i + forTkn.length());

            if(i > 3 && expression.substring(i - 3, i + forTkn.length()).equals(endTkn))
            {
                continue;
            }
            else if(sub.equals(forTkn))
            {
                skip++;
            }
        }

        for(int i=0;i<=expression.length() - endTkn.length();i++)
        {
            String sub = expression.substring(i, i + endTkn.length());

            if(sub.equals(endTkn))
            {
                if(skip > 1)
                {
                    skip--;
                }
                else
                {
                    newExp = expression.substring(0, i + endTkn.length());
                    return newExp;
                }
            }
        }

        return newExp;
    }

    private void assignment(String key, String operator, String expression)
    {
        Object value = null;

        if(variables.containsKey(expression))
        {
            value = variables.get(expression);
        }
        else if(expression.startsWith("\""))
        {
            String str = expression;

            int index = str.indexOf("=");
            expression = str.substring(index + 1, str.length() - 1);
            expression = expression.trim().replaceAll("\"", "");

            value = new String(expression);
        }
        else if(isInteger(expression))
        {
            value = new Integer(expression);
        }
        else
        {
            undeclaredVariable(key);
        }

        assignmentOperation(key, value, operator);
    }

    private void print(String statement)
    {
        String parts[] = statement.split(" ");
        String key = parts[0];

        undeclaredVariable(key);

        Object value = variables.get(key);
        
        if(value instanceof String)
        {
        	System.out.println(key + "=\"" + value.toString() + "\"");
        }
        else
        {
            System.out.println(key + "=" + value.toString());
        }
    }

    private void assignmentOperation(String key, Object value, String operator)
    {
        if(!variables.containsKey(key))
        {
            variables.put(key, value);
        }
        else
        {
            complexAssignment(key, value, operator);
        }
    }

    private void complexAssignment(String key, Object value, String operator)
    {
        Object keyValue = variables.get(key);

        if(operator.equals("="))
        {
            variables.put(key, value);
        }
        else if(operator.equals("*="))
        {
            operatorTypeError(keyValue, value);
            multiplyAssignment(key, keyValue, value);
        }
        else if(operator.equals("+="))
        {
            incompatibleTypeError(keyValue, value);
            additionAssignment(key, keyValue, value);
        }
        else if(operator.equals("-="))
        {
            operatorTypeError(variables.get(key), value);
            subtractAssignment(key, keyValue, value);
        }
    }

    private void subtractAssignment(String key, Object val1, Object val2)
    {
        int num = (int) val1 - (int) val2;
        variables.put(key, num);
    }

    private void multiplyAssignment(String key, Object val1, Object val2)
    {
        int num = (int) val1 * (int) val2;
        variables.put(key, num);
    }

    private void additionAssignment(String key, Object val1, Object val2)
    {
        if(val1 instanceof Integer)
        {
            int num = (int) val1 + (int) val2;
            variables.put(key, num);
        }
        else if(val1 instanceof String)
        {
            String str = val1.toString() + val2.toString();
            variables.put(key, str);
        }
    }

    private void operatorTypeError(Object val1, Object val2)
    {
        if(!(val1 instanceof Integer) || !(val2 instanceof Integer))
        {
            System.out.println("RUNTIME ERROR: LINE " + (currentLine + 1));
            System.exit(0);
        }

    }

    private void incompatibleTypeError(Object val1, Object val2)
    {
        if(!(((val1 instanceof String) && (val2 instanceof String))
        || ((val1 instanceof Integer) && (val2 instanceof Integer))))
        {
            System.out.println("RUNTIME ERROR: LINE " + (currentLine + 1));
            System.exit(0);
        }
    }

    private void undeclaredVariable(String key)
    {
        if(!variables.containsKey(key))
        {
            System.out.println("RUNTIME ERROR: LINE " + (currentLine + 1));
            System.exit(0);
        }
    }

    private boolean isInteger(String str)
    {
        for(int i=0;i<str.length();i++)
        {
            char c = str.charAt(i);

            if(!Character.isDigit(c))
            {
                return false; 
            }
        }

        return true;
    }
}
