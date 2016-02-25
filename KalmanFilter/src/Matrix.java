import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.ArrayList;

class SVDResult
{
	Matrix u;
	Matrix v;
	double[] diag;
}

/** This stores a matrix, A.K.A. data set, A.K.A. table. Each element is
 *  represented as a double value. Nominal values are represented using their
 *  corresponding zero-indexed enumeration value. For convenience,
 *  the matrix also stores some meta-data which describes the columns (or attributes)
 *  in the matrix. */
public class Matrix
{
	/** Used to represent elements in the matrix for which the value is not known. */
	public static final double UNKNOWN_VALUE = -1e308; 

	// Data
	private ArrayList<double[]> m_data = new ArrayList<double[]>(); //matrix elements

	// Meta-data
	private String m_filename;                          // the name of the file
	private ArrayList<String> m_attr_name;                 // the name of each attribute (or column)
	private ArrayList<Map<String, Integer>> m_str_to_enum; // value to enumeration
	private ArrayList<Map<Integer, String>> m_enum_to_str; // enumeration to value

	/** Creates a 0x0 matrix. (Next, to give this matrix some dimensions, you should call:
	 *     loadARFF
	 *     setSize
	 *     addColumn, or
	 *     copyMetaData */
	@SuppressWarnings("unchecked")
	public Matrix() 
	{
		this.m_filename    = "";
		this.m_attr_name   = new ArrayList<String>();
		this.m_str_to_enum = new ArrayList<Map<String, Integer>>();
		this.m_enum_to_str = new ArrayList<Map<Integer, String>>();
	}

	public Matrix(int rows, int cols)
	{
		this.m_filename    = "";
		this.m_attr_name   = new ArrayList<String>();
		this.m_str_to_enum = new ArrayList<Map<String, Integer>>();
		this.m_enum_to_str = new ArrayList<Map<Integer, String>>();
		setSize(rows, cols);
	}

	public Matrix(Matrix that)
	{
		m_filename = that.m_filename;
		m_attr_name = that.m_attr_name;
		m_str_to_enum = that.m_str_to_enum;
		m_enum_to_str = that.m_enum_to_str;
		setSize(0, that.cols());
		copyBlock(that, 0, 0, that.rows(), that.cols());
	}

	/** Loads the matrix from an ARFF file */
	public void loadARFF(String filename)
	{
		Map<String, Integer> tempMap  = new HashMap<String, Integer>(); //temp map for int->string map (attrInts)
		Map<Integer, String> tempMapS = new HashMap<Integer, String>(); //temp map for string->int map (attrString)
		
		int attrCount                 = 0; // Count number of attributes
		int lineNum                   = 0; // Used for exceptions
		
		Scanner s = null;
		m_attr_name.clear();
		m_str_to_enum.clear();
		m_enum_to_str.clear();
		try
		{
			s = new Scanner(new File(filename));
			while (s.hasNextLine())
			{
				lineNum++;
				String line  = s.nextLine().trim();
				String upper = line.toUpperCase();

				if (upper.startsWith("@RELATION"))
					m_filename = line.split(" ")[1];
				else if (upper.startsWith("@ATTRIBUTE"))
				{
					String[] pieces = line.split("\\s+");
					m_attr_name.add(pieces[1]);
					
					tempMap.clear();
					tempMapS.clear();
					
					// If the attribute is nominal
					if (pieces[2].startsWith("{"))
					{
						// Splits this string based on curly brackets or commas
						String[] attributeNames = pieces[2].split("[{},]");
						int valCount = 0;
						
						for (String attribute : attributeNames)
						{
							if (!attribute.equals("")) // Ignore empty strings
							{
								tempMapS.put(valCount, attribute);
								tempMap.put(attribute, valCount++);
							}
						}
					}
					
					// The attribute is continuous if it wasn't picked up in the previous "if" statement
					
					m_str_to_enum.add(new HashMap<String, Integer>(tempMap));
					m_enum_to_str.add(new HashMap<Integer, String>(tempMapS));
					
					attrCount++;
				}
				else if (upper.startsWith("@DATA"))
				{
					m_data.clear();

					while (s.hasNextLine())
					{
						double[] temp = new double[attrCount];

						lineNum++;
						line  = s.nextLine().trim();
						
						if (line.startsWith("%") || line.isEmpty()) continue;
						String[] pieces = line.split(",");
						
						if (pieces.length < attrCount) throw new IllegalArgumentException("Expected more elements on line: " + lineNum + ".");
						
						for (int i = 0; i < attrCount; i++)
						{
							int vals   = valueCount(i);
							String val = pieces[i];
							
							// Unknown values are always set to UNKNOWN_VALUE
							if (val.equals("?"))
							{
								temp[i] = UNKNOWN_VALUE;
								continue;
							}
		
							// If the attribute is nominal
							if (vals > 0)
							{
								Map<String, Integer> enumMap = m_str_to_enum.get(i);
								if (!enumMap.containsKey(val))
									throw new IllegalArgumentException("Unrecognized enumeration value " + val + " on line: " + lineNum + ".");
									
								temp[i] = (double)enumMap.get(val);
							}
							else
								temp[i] = Double.parseDouble(val); // The attribute is continuous
						}
						
						m_data.add(temp);
					}
				}
			}
		}
		catch (FileNotFoundException e)
		{
			throw new IllegalArgumentException("Failed to open file: " + filename + ".");
		}
		finally
		{
			s.close();
		}
	}

	public void print() {
		for(int j = 0; j < rows(); j++) {
			Vec.println(row(j));
		}
	}

	/** Saves the matrix to an ARFF file */
	public void saveARFF(String filename) throws Exception
	{		
		PrintWriter os = null;
		
		try
		{
			os = new PrintWriter(filename);
			// Print the relation name, if one has been provided ('x' is default)
			os.print("@RELATION ");
			os.println(m_filename.isEmpty() ? "x" : m_filename);
			
			// Print each attribute in order
			for (int i = 0; i < m_attr_name.size(); i++)
			{
				os.print("@ATTRIBUTE ");
				
				String attributeName = m_attr_name.get(i);
				os.print(attributeName.isEmpty() ? "x" : attributeName);
				
				int vals = valueCount(i);
				
				if (vals == 0) os.println(" REAL");
				else
				{
					os.print(" {");
					for (int j = 0; j < vals; j++)
					{
						os.print(attrValue(i, j));
						if (j + 1 < vals) os.print(",");
					}
					os.println("}");
				}
			}
			
			// Print the data
			os.println("@DATA");
			for (int i = 0; i < rows(); i++)
			{
				double[] row = m_data.get(i);
				for (int j = 0; j < cols(); j++)
				{
					if (row[j] == UNKNOWN_VALUE)
						os.print("?");
					else
					{
						int vals = valueCount(j);
						if (vals == 0) os.print(row[j]);
						else
						{
							int val = (int)row[j];
							if (val >= vals) throw new Exception("Value out of range.");
							os.print(attrValue(j, val));
						}
					}
					
					if (j + 1 < cols())	os.print(",");
				}
				os.println();
			}
		}
		catch (FileNotFoundException e)
		{
			throw new Exception("Error creating file: " + filename + ".");
		}
		finally
		{
			os.close();
		}
	}

	/** Makes a rows-by-columns matrix of *ALL CONTINUOUS VALUES*.
	 *  This method wipes out any data currently in the matrix. It also
	 *  wipes out any meta-data. */
	public void setSize(int rows, int cols)
	{
		m_data.clear();

		// Set the meta-data
		m_filename = "";
		m_attr_name.clear();
		m_str_to_enum.clear();
		m_enum_to_str.clear();

		// Make space for each of the columns, then each of the rows
		newColumns(cols);
		newRows(rows);
	}

	/** Clears this matrix and copies the meta-data from that matrix.
	 *  In other words, it makes a zero-row matrix with the same number
	 *  of columns as "that" matrix. You will need to call newRow or newRows
	 *  to give the matrix some rows. */
	@SuppressWarnings("unchecked")
	public void copyMetaData(Matrix that)
	{
		m_data.clear();
		m_attr_name = new ArrayList<String>(that.m_attr_name);
		
		// Make a deep copy of that.m_str_to_enum
		m_str_to_enum = new ArrayList<Map<String, Integer>>();
		for (Map<String, Integer> map : that.m_str_to_enum)
		{
			Map<String, Integer> temp = new HashMap<String, Integer>();
			for (Map.Entry<String, Integer> entry : map.entrySet())
				temp.put(entry.getKey(), entry.getValue());
			
			m_str_to_enum.add(temp);
		}
		
		// Make a deep copy of that.m_enum_to_string
		m_enum_to_str = new ArrayList<Map<Integer, String>>();
		for (Map<Integer, String> map : that.m_enum_to_str)
		{
			Map<Integer, String> temp = new HashMap<Integer, String>();
			for (Map.Entry<Integer, String> entry : map.entrySet())
				temp.put(entry.getKey(), entry.getValue());
			
			m_enum_to_str.add(temp);
		}
	}

	/** Adds a column to this matrix with the specified number of values. (Use 0 for
	 *  a continuous attribute.) This method also sets the number of rows to 0, so
	 *  you will need to call newRow or newRows when you are done adding columns. */
	public void newColumn(int vals)
	{
		m_data.clear();
		String name = "col_" + cols();
		
		m_attr_name.add(name);
		
		Map<String, Integer> temp_str_to_enum = new HashMap<String, Integer>();
		Map<Integer, String> temp_enum_to_str = new HashMap<Integer, String>();
		
		for (int i = 0; i < vals; i++)
		{
			String sVal = "val_" + i;
			temp_str_to_enum.put(sVal, i);
			temp_enum_to_str.put(i, sVal);
		}
		
		m_str_to_enum.add(temp_str_to_enum);
		m_enum_to_str.add(temp_enum_to_str);
	}
	
	/** Adds a column to this matrix with 0 values (continuous data). */
	public void newColumn()
	{
		this.newColumn(0);
	}
	
	/** Adds n columns to this matrix, each with 0 values (continuous data). */
	public void newColumns(int n)
	{
		for (int i = 0; i < n; i++)
			newColumn();
	}
	
	/** Adds one new row to this matrix. Returns a reference to the new row. */
	public double[] newRow()
	{
		int c = cols();
		if (c == 0)
			throw new IllegalArgumentException("You must add some columns before you add any rows.");
		double[] newRow = new double[c];
		m_data.add(newRow);
		return newRow;
	}
	
	/** Adds 'n' new rows to this matrix */
	public void newRows(int n)
	{
		for (int i = 0; i < n; i++)
			newRow();
	}
	
	/** Returns the number of rows in the matrix */
	public int rows() { return m_data.size(); }
	
	/** Returns the number of columns (or attributes) in the matrix */
	public int cols() { return m_attr_name.size(); }
	
	/** Returns the name of the specified attribute */
	public String attrName(int col) { return m_attr_name.get(col); }
	
	/** Returns the name of the specified value */
	public String attrValue(int attr, int val)
	{		
		String value = m_enum_to_str.get(attr).get(val);
		if (value == null)
			throw new IllegalArgumentException("No name.");
		else return value;
	}
	
	/** Returns a reference to the specified row */
	public double[] row(int index) { return m_data.get(index); }
	
	/** Swaps the positions of the two specified rows */
	public void swapRows(int a, int b)
	{
		double[] temp = m_data.get(a);
		m_data.set(a, m_data.get(b));
		m_data.set(b, temp);
	}

	/** Swaps the the two specified columns */
	public void swapColumns(int a, int b)
	{
		for(int i = 0; i < rows(); i++)
		{
			double[] r = row(i);
			double t = r[a];
			r[a] = r[b];
			r[b] = t;
		}
		String t = m_attr_name.get(a);
		m_attr_name.set(a, m_attr_name.get(b));
		m_attr_name.set(b, t);
		Map<String, Integer> t2 = m_str_to_enum.get(a);
		m_str_to_enum.set(a, m_str_to_enum.get(b));
		m_str_to_enum.set(b, t2);
		Map<Integer, String> t3 = m_enum_to_str.get(a);
		m_enum_to_str.set(a, m_enum_to_str.get(b));
		m_enum_to_str.set(b, t3);
	}

	/** Returns the number of values associated with the specified attribute (or column)
	 *  0 = continuous, 2 = binary, 3 = trinary, etc. */
	public int valueCount(int attr) { return m_enum_to_str.get(attr).size(); }
	
	/** Returns the mean of the elements in the specified column. (Elements with the value UNKNOWN_VALUE are ignored.) */
	public double columnMean(int col)
	{
		double sum = 0.0;
		int count = 0;
		for (double[] list : m_data)
		{
			double val = list[col];
			if (val != UNKNOWN_VALUE)
			{
				sum += val;
				count++;
			}
		}
		
		return sum / count;
	}
	
	/** Returns the minimum element in the specified column. (Elements with the value UNKNOWN_VALUE are ignored.) */
	public double columnMin(int col)
	{
		double min = Double.MAX_VALUE;
		for (double[] list : m_data)
		{
			double val = list[col];
			if (val != UNKNOWN_VALUE)
				min = Math.min(min, val);
		}
		
		return min;
	}
	
	/** Returns the maximum element in the specifed column. (Elements with the value UNKNOWN_VALUE are ignored.) */
	public double columnMax(int col)
	{
		double max = Double.MIN_VALUE;
		for (double[] list : m_data)
		{
			double val = list[col];
			if (val != UNKNOWN_VALUE)
				max = Math.max(max, val);
		}
		
		return max;
	}
	
	/** Returns the most common value in the specified column. (Elements with the value UNKNOWN_VALUE are ignored.) */
	public double mostCommonValue(int col)
	{
		Map<Double, Integer> counts = new HashMap<Double, Integer>();
		for (double[] list : m_data)
		{
			double val = list[col];
			if (val != UNKNOWN_VALUE)
			{
				Integer result = counts.get(val);
				if (result == null) result = 0;
				
				counts.put(val, result + 1);
			}
		}
		
		int valueCount = 0;
		double value   = 0;
		for (Map.Entry<Double, Integer> entry : counts.entrySet())
		{
			if (entry.getValue() > valueCount)
			{
				value      = entry.getKey();
				valueCount = entry.getValue();
			}
		}
		
		return value;
	}
	
	/** Copies the specified rectangular portion of that matrix, and adds it to the bottom of this matrix.
	 *  (If colCount does not match the number of columns in this matrix, then this matrix will be cleared first.) */
	public void copyBlock(Matrix that, int rowBegin, int colBegin, int rowCount, int colCount)
	{
		if (rowBegin + rowCount > that.rows() || colBegin + colCount > that.cols())
			throw new IllegalArgumentException("Out of range.");
		
		// Copy the specified region of meta-data
		setSize(0, colCount);
		for (int i = 0; i < colCount; i++)
		{
			m_attr_name.set  (i, that.m_attr_name.get(colBegin + i));
			m_str_to_enum.set(i, that.m_str_to_enum.get(colBegin + i));
			m_enum_to_str.set(i, that.m_enum_to_str.get(colBegin + i));
		}
		
		// Copy the specified region of data		
		for (int i = 0; i < rowCount; i++)
		{
			double[] source = that.row(rowBegin + i);
			double[] newrow = newRow();
			for(int j = 0; j < colCount; j++)
				newrow[j] = source[colBegin + j];
		}
	}
	
	/** Sets every element in the matrix to the specified value. */
	public void setAll(double val)
	{
		for (double[] list : m_data) {
			for(int i = 0; i < list.length; i++)
				list[i] = val;
		}
	}

	/** Sets this to the identity matrix. */
	public void setToIdentity()
	{
		setAll(0.0);
		int m = Math.min(cols(), rows());
		for(int i = 0; i < m; i++)
			row(i)[i] = 1.0;
	}

	/** Throws an exception if that has a different number of columns than
	 *  this, or if one of its columns has a different number of values. */
	public void checkCompatibility(Matrix that)
	{
		int c = cols();
		if (that.cols() != c)
			throw new IllegalArgumentException("Matrices have different number of columns.");
		
		for (int i = 0; i < c; i++)
		{
			if (valueCount(i) != that.valueCount(i))
				throw new IllegalArgumentException("Column " + i + " has mis-matching number of values.");
		}
	}

	double Matrix_pythag(double a, double b)
	{
		double at = Math.abs(a);
		double bt = Math.abs(b);
		if(at > bt)
		{
			double ct = bt / at;
			return at * Math.sqrt(1.0 + ct * ct);
		}
		else if(bt > 0.0)
		{
			double ct = at / bt;
			return bt * Math.sqrt(1.0 + ct * ct);
		}
		else
			return 0.0;
	}

	double Matrix_safeDivide(double n, double d)
	{
		if(d == 0.0 && n == 0.0)
			return 0.0;
		else
		{
			double t = n / d;
			//GAssert(t > -1e200, "prob");
			return t;
		}
	}

 	double Matrix_takeSign(double a, double b)
	{
		return (b >= 0.0 ? Math.abs(a) : -Math.abs(a));
	}

	void fixNans()
	{
		int colCount = cols();
		for(int i = 0; i < rows(); i++)
		{
			double[] pRow = row(i);
			for(int j = 0; j < colCount; j++)
			{
				if(Double.isNaN(pRow[j]))
					pRow[j] = (i == j ? 1.0 : 0.0);
			}
		}
	}

	Matrix transpose()
	{
		Matrix res = new Matrix(cols(), rows());
		for(int i = 0; i < rows(); i++)
		{
			for(int j = 0; j < cols(); j++)
				res.row(j)[i] = row(i)[j];
		}
		return res;
	}
	
	//Needs testing to ensure proper functioning
	static Matrix add(Matrix a, Matrix b){
		Matrix sum = new Matrix(a.rows(), a.cols());
		for(int i = 0; i < a.rows(); i++)
			for(int j = 0; j < a.cols(); j++)
				sum.row(i)[j] = a.row(i)[j] + b.row(i)[j];
		return sum;
	}

	static Matrix multiply(Matrix a, Matrix b, boolean transposeA, boolean transposeB)
	{
		Matrix res = new Matrix(transposeA ? a.cols() : a.rows(), transposeB ? b.rows() : b.cols());
		if(transposeA)
		{
			if(transposeB)
			{
				if(a.rows() != b.cols())
					throw new IllegalArgumentException("No can do");
				for(int i = 0; i < res.rows(); i++)
				{
					for(int j = 0; j < res.cols(); j++)
					{
						double d = 0.0;
						for(int k = 0; k < a.cols(); k++)
							d += a.row(k)[i] * b.row(j)[k];
						res.row(i)[j] = d;
					}
				}
			}
			else
			{
				if(a.rows() != b.rows())
					throw new IllegalArgumentException("No can do");
				for(int i = 0; i < res.rows(); i++)
				{
					for(int j = 0; j < res.cols(); j++)
					{
						double d = 0.0;
						for(int k = 0; k < a.cols(); k++)
							d += a.row(k)[i] * b.row(k)[j];
						res.row(i)[j] = d;
					}
				}
			}
		}
		else
		{
			if(transposeB)
			{
				if(a.cols() != b.cols())
					throw new IllegalArgumentException("No can do");
				for(int i = 0; i < res.rows(); i++)
				{
					for(int j = 0; j < res.cols(); j++)
					{
						double d = 0.0;
						for(int k = 0; k < a.cols(); k++)
							d += a.row(i)[k] * b.row(j)[k];
						res.row(i)[j] = d;
					}
				}
			}
			else
			{
				if(a.cols() != b.rows())
					throw new IllegalArgumentException("No can do");
				for(int i = 0; i < res.rows(); i++)
				{
					for(int j = 0; j < res.cols(); j++)
					{
						double d = 0.0;
						for(int k = 0; k < a.cols(); k++)
							d += a.row(i)[k] * b.row(k)[j];
						res.row(i)[j] = d;
					}
				}
			}
		}
		return res;
	}

	SVDResult singularValueDecompositionHelper(boolean throwIfNoConverge, int maxIters)
	{
		int m = rows();
		int n = cols();
		if(m < n)
			throw new IllegalArgumentException("Expected at least as many rows as columns");
		int j, k;
		int l = 0;
		int p, q;
		double c, f, h, s, x, y, z;
		double norm = 0.0;
		double g = 0.0;
		double scale = 0.0;
		SVDResult res = new SVDResult();
		Matrix pU = new Matrix(m, m);
		res.u = pU;
		pU.setAll(0.0);
		for(int i = 0; i < m; i++)
		{
			double[] rOut = pU.row(i);
			double[] rIn = row(i);
			for(j = 0; j < n; j++)
				rOut[j] = rIn[j];
		}
		double[] pSigma = new double[n];
		res.diag = pSigma;
		Matrix pV = new Matrix(n, n);
		res.v = pV;
		pV.setAll(0.0);
		double[] temp = new double[n];

		// Householder reduction to bidiagonal form
		for(int i = 0; i < n; i++)
		{
			// Left-hand reduction
			temp[i] = scale * g;
			l = i + 1;
			g = 0.0;
			s = 0.0;
			scale = 0.0;
			if(i < m)
			{
				for(k = i; k < m; k++)
					scale += Math.abs(pU.row(k)[i]);
				if(scale != 0.0)
				{
					for(k = i; k < m; k++)
					{
						pU.row(k)[i] = Matrix_safeDivide(pU.row(k)[i], scale);
						double t = pU.row(k)[i];
						s += t * t;
					}
					f = pU.row(i)[i];
					g = -Matrix_takeSign(Math.sqrt(s), f);
					h = f * g - s;
					pU.row(i)[i] = f - g;
					if(i != n - 1)
					{
						for(j = l; j < n; j++)
						{
							s = 0.0;
							for(k = i; k < m; k++)
								s += pU.row(k)[i] * pU.row(k)[j];
							f = Matrix_safeDivide(s, h);
							for(k = i; k < m; k++)
								pU.row(k)[j] += f * pU.row(k)[i];
						}
					}
					for(k = i; k < m; k++)
						pU.row(k)[i] *= scale;
				}
			}
			pSigma[i] = scale * g;

			// Right-hand reduction
			g = 0.0;
			s = 0.0;
			scale = 0.0;
			if(i < m && i != n - 1)
			{
				for(k = l; k < n; k++)
					scale += Math.abs(pU.row(i)[k]);
				if(scale != 0.0)
				{
					for(k = l; k < n; k++)
					{
						pU.row(i)[k] = Matrix_safeDivide(pU.row(i)[k], scale);
						double t = pU.row(i)[k];
						s += t * t;
					}
					f = pU.row(i)[l];
					g = -Matrix_takeSign(Math.sqrt(s), f);
					h = f * g - s;
					pU.row(i)[l] = f - g;
					for(k = l; k < n; k++)
						temp[k] = Matrix_safeDivide(pU.row(i)[k], h);
					if(i != m - 1)
					{
						for(j = l; j < m; j++)
						{
							s = 0.0;
							for(k = l; k < n; k++)
								s += pU.row(j)[k] * pU.row(i)[k];
							for(k = l; k < n; k++)
								pU.row(j)[k] += s * temp[k];
						}
					}
					for(k = l; k < n; k++)
						pU.row(i)[k] *= scale;
				}
			}
			norm = Math.max(norm, Math.abs(pSigma[i]) + Math.abs(temp[i]));
		}

		// Accumulate right-hand transform
		for(int i = n - 1; i >= 0; i--)
		{
			if(i < n - 1)
			{
				if(g != 0.0)
				{
					for(j = l; j < n; j++)
						pV.row(i)[j] = Matrix_safeDivide(Matrix_safeDivide(pU.row(i)[j], pU.row(i)[l]), g); // (double-division to avoid underflow)
					for(j = l; j < n; j++)
					{
						s = 0.0;
						for(k = l; k < n; k++)
							s += pU.row(i)[k] * pV.row(j)[k];
						for(k = l; k < n; k++)
							pV.row(j)[k] += s * pV.row(i)[k];
					}
				}
				for(j = l; j < n; j++)
				{
					pV.row(i)[j] = 0.0;
					pV.row(j)[i] = 0.0;
				}
			}
			pV.row(i)[i] = 1.0;
			g = temp[i];
			l = i;
		}

		// Accumulate left-hand transform
		for(int i = n - 1; i >= 0; i--)
		{
			l = i + 1;
			g = pSigma[i];
			if(i < n - 1)
			{
				for(j = l; j < n; j++)
					pU.row(i)[j] = 0.0;
			}
			if(g != 0.0)
			{
				g = Matrix_safeDivide(1.0, g);
				if(i != n - 1)
				{
					for(j = l; j < n; j++)
					{
						s = 0.0;
						for(k = l; k < m; k++)
							s += pU.row(k)[i] * pU.row(k)[j];
						f = Matrix_safeDivide(s, pU.row(i)[i]) * g;
						for(k = i; k < m; k++)
							pU.row(k)[j] += f * pU.row(k)[i];
					}
				}
				for(j = i; j < m; j++)
					pU.row(j)[i] *= g;
			}
			else
			{
				for(j = i; j < m; j++)
					pU.row(j)[i] = 0.0;
			}
			pU.row(i)[i] += 1.0;
		}

		// Diagonalize the bidiagonal matrix
		for(k = n - 1; k >= 0; k--) // For each singular value
		{
			for(int iter = 1; iter <= maxIters; iter++)
			{
				// Test for splitting
				boolean flag = true;
				q = 0;
				for(l = k; l >= 0; l--)
				{
					q = l - 1;
					if(Math.abs(temp[l]) + norm == norm)
					{
						flag = false;
						break;
					}
					if(Math.abs(pSigma[q]) + norm == norm)
						break;
				}

				if(flag)
				{
					c = 0.0;
					s = 1.0;
					for(int i = l; i <= k; i++)
					{
						f = s * temp[i];
						temp[i] *= c;
						if(Math.abs(f) + norm == norm)
							break;
						g = pSigma[i];
						h = Matrix_pythag(f, g);
						pSigma[i] = h;
						h = Matrix_safeDivide(1.0, h);
						c = g * h;
						s = -f * h;
						for(j = 0; j < m; j++)
						{
							y = pU.row(j)[q];
							z = pU.row(j)[i];
							pU.row(j)[q] = y * c + z * s;
							pU.row(j)[i] = z * c - y * s;
						}
					}
				}

				z = pSigma[k];
				if(l == k)
				{
					// Detect convergence
					if(z < 0.0)
					{
						// Singular value should be positive
						pSigma[k] = -z;
						for(j = 0; j < n; j++)
							pV.row(k)[j] *= -1.0;
					}
					break;
				}
				if(throwIfNoConverge && iter >= maxIters)
					throw new IllegalArgumentException("failed to converge");

				// Shift from bottom 2x2 minor
				x = pSigma[l];
				q = k - 1;
				y = pSigma[q];
				g = temp[q];
				h = temp[k];
				f = Matrix_safeDivide(((y - z) * (y + z) + (g - h) * (g + h)), (2.0 * h * y));
				g = Matrix_pythag(f, 1.0);
				f = Matrix_safeDivide(((x - z) * (x + z) + h * (Matrix_safeDivide(y, (f + Matrix_takeSign(g, f))) - h)), x);

				// QR transform
				c = 1.0;
				s = 1.0;
				for(j = l; j <= q; j++)
				{
					int i = j + 1;
					g = temp[i];
					y = pSigma[i];
					h = s * g;
					g = c * g;
					z = Matrix_pythag(f, h);
					temp[j] = z;
					c = Matrix_safeDivide(f, z);
					s = Matrix_safeDivide(h, z);
					f = x * c + g * s;
					g = g * c - x * s;
					h = y * s;
					y = y * c;
					for(p = 0; p < n; p++)
					{
						x = pV.row(j)[p];
						z = pV.row(i)[p];
						pV.row(j)[p] = x * c + z * s;
						pV.row(i)[p] = z * c - x * s;
					}
					z = Matrix_pythag(f, h);
					pSigma[j] = z;
					if(z != 0.0)
					{
						z = Matrix_safeDivide(1.0, z);
						c = f * z;
						s = h * z;
					}
					f = c * g + s * y;
					x = c * y - s * g;
					for(p = 0; p < m; p++)
					{
						y = pU.row(p)[j];
						z = pU.row(p)[i];
						pU.row(p)[j] = y * c + z * s;
						pU.row(p)[i] = z * c - y * s;
					}
				}
				temp[l] = 0.0;
				temp[k] = f;
				pSigma[k] = x;
			}
		}

		// Sort the singular values from largest to smallest
		for(int i = 1; i < n; i++)
		{
			for(j = i; j > 0; j--)
			{
				if(pSigma[j - 1] >= pSigma[j])
					break;
				pU.swapColumns(j - 1, j);
				pV.swapRows(j - 1, j);
				double tmp = pSigma[j];
				pSigma[j] = pSigma[j - 1];
				pSigma[j - 1] = tmp;
			}
		}

		// Return results
		pU.fixNans();
		pV.fixNans();
		return res;
	}

	Matrix pseudoInverse()
	{
		SVDResult res;
		int colCount = cols();
		int rowCount = rows();
		if(rowCount < colCount)
		{
			Matrix pTranspose = transpose();
			res = pTranspose.singularValueDecompositionHelper(false, 80);
		}
		else
			res = singularValueDecompositionHelper(false, 80);
		Matrix sigma = new Matrix(rowCount < colCount ? colCount : rowCount, rowCount < colCount ? rowCount : colCount);
		sigma.setAll(0.0);
		int m = Math.min(rowCount, colCount);
		for(int i = 0; i < m; i++)
		{
			if(Math.abs(res.diag[i]) > 1e-9)
				sigma.row(i)[i] = Matrix_safeDivide(1.0, res.diag[i]);
			else
				sigma.row(i)[i] = 0.0;
		}
		Matrix pT = Matrix.multiply(res.u, sigma, false, false);
		if(rowCount < colCount)
			return Matrix.multiply(pT, res.v, false, false);
		else
			return Matrix.multiply(res.v, pT, true, true);
	}

}
