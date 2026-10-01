import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { biomarkerApi } from '../api/biomarkerApi';
import { BiomarkerHistory, Trajectory } from '../types';
import { 
  TrendingUp, 
  ArrowLeft, 
  Activity, 
  AlertCircle, 
  ArrowUpRight, 
  ArrowDownRight, 
  Minus
} from 'lucide-react';
import { 
  ResponsiveContainer, 
  LineChart, 
  Line, 
  XAxis, 
  YAxis, 
  Tooltip, 
  CartesianGrid 
} from 'recharts';

const getTrajectoryBadge = (trajectory: Trajectory, delta?: number) => {
  switch (trajectory) {
    case 'RISING':
      return (
        <span className="inline-flex items-center space-x-1 px-2.5 py-1 rounded-full text-xs font-bold bg-amber-100 text-amber-800">
          <ArrowUpRight className="w-3.5 h-3.5" />
          <span>Rising ({delta && delta > 0 ? `+${delta.toFixed(2)}` : delta})</span>
        </span>
      );
    case 'FALLING':
      return (
        <span className="inline-flex items-center space-x-1 px-2.5 py-1 rounded-full text-xs font-bold bg-blue-100 text-blue-800">
          <ArrowDownRight className="w-3.5 h-3.5" />
          <span>Falling ({delta?.toFixed(2)})</span>
        </span>
      );
    case 'STABLE':
      return (
        <span className="inline-flex items-center space-x-1 px-2.5 py-1 rounded-full text-xs font-bold bg-emerald-100 text-emerald-800">
          <Minus className="w-3.5 h-3.5" />
          <span>Stable (&plusmn;5%)</span>
        </span>
      );
    default:
      return (
        <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-slate-100 text-slate-600">
          Insufficient Data
        </span>
      );
  }
};

export const BiomarkerDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [history, setHistory] = useState<BiomarkerHistory | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;

    const loadHistory = async () => {
      setLoading(true);
      try {
        const data = await biomarkerApi.getBiomarkerHistory(id);
        setHistory(data);
      } catch (err: any) {
        setError(err.response?.data?.message || 'Failed to load biomarker history.');
      } finally {
        setLoading(false);
      }
    };

    loadHistory();
  }, [id]);

  if (loading) {
    return (
      <div className="py-24 flex flex-col items-center justify-center space-y-3">
        <div className="w-10 h-10 border-4 border-emerald-600 border-t-transparent rounded-full animate-spin"></div>
        <p className="text-xs text-slate-500 font-medium">Calculating deterministic longitudinal metrics...</p>
      </div>
    );
  }

  if (error || !history) {
    return (
      <div className="py-16 text-center">
        <AlertCircle className="w-12 h-12 text-rose-500 mx-auto mb-3" />
        <h3 className="text-base font-bold text-slate-900">Unable to load trend analysis</h3>
        <p className="text-xs text-slate-500 mt-1 mb-4">{error || 'Biomarker history not found'}</p>
        <Link to="/biomarkers" className="text-xs font-semibold text-emerald-700 hover:underline">
          &larr; Return to Biomarker Catalog
        </Link>
      </div>
    );
  }

  // Format points for Recharts
  const chartData = history.dataPoints.map((dp) => ({
    date: new Date(dp.timestamp).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: '2-digit' }),
    value: dp.observedValue,
    unit: dp.unit,
    status: dp.status,
  }));

  const stats = history.statistics;

  return (
    <div className="space-y-8">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-slate-200">
        <div>
          <Link
            to="/biomarkers"
            className="inline-flex items-center space-x-1 text-xs font-semibold text-slate-500 hover:text-emerald-700 mb-2"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Back to Catalog</span>
          </Link>
          <div className="flex items-center space-x-3">
            <h1 className="text-2xl font-bold text-slate-900 tracking-tight">
              {history.canonicalName}
            </h1>
            <span className="px-2.5 py-0.5 rounded-md bg-emerald-50 text-emerald-800 text-xs font-mono font-bold">
              {history.standardUnit}
            </span>
          </div>
          <p className="text-xs text-slate-500 mt-1">
            Category: {history.category || 'General'} {history.codeLoinc ? `• LOINC: ${history.codeLoinc}` : ''}
          </p>
        </div>

        <div>
          {getTrajectoryBadge(stats.trajectory, stats.delta)}
        </div>
      </div>

      {/* Statistical Summary Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-xs">
          <p className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Latest Value</p>
          <h4 className="text-xl font-bold text-slate-900 mt-1">
            {stats.latestValue !== undefined ? `${stats.latestValue} ${history.standardUnit}` : 'N/A'}
          </h4>
        </div>

        <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-xs">
          <p className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Previous Value</p>
          <h4 className="text-xl font-bold text-slate-700 mt-1">
            {stats.previousValue !== undefined ? `${stats.previousValue} ${history.standardUnit}` : 'N/A'}
          </h4>
        </div>

        <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-xs">
          <p className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Historical Min</p>
          <h4 className="text-xl font-bold text-slate-900 mt-1">
            {stats.minValue !== undefined ? `${stats.minValue} ${history.standardUnit}` : 'N/A'}
          </h4>
        </div>

        <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-xs">
          <p className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Historical Max</p>
          <h4 className="text-xl font-bold text-slate-900 mt-1">
            {stats.maxValue !== undefined ? `${stats.maxValue} ${history.standardUnit}` : 'N/A'}
          </h4>
        </div>
      </div>

      {/* Interactive Longitudinal Line Chart */}
      <div className="bg-white p-6 rounded-3xl border border-slate-200 shadow-xs space-y-4">
        <div className="flex items-center justify-between pb-2 border-b border-slate-100">
          <div className="flex items-center space-x-2">
            <TrendingUp className="w-5 h-5 text-emerald-600" />
            <h3 className="text-sm font-bold text-slate-900">Longitudinal Trend Curve</h3>
          </div>
          <span className="text-xs text-slate-400">
            {history.dataPoints.length} chronological data point(s)
          </span>
        </div>

        {chartData.length > 0 ? (
          <div className="h-72 w-full pt-4">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={chartData} margin={{ top: 10, right: 30, left: 10, bottom: 20 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                <XAxis dataKey="date" stroke="#94a3b8" fontSize={11} tickMargin={8} />
                <YAxis stroke="#94a3b8" fontSize={11} domain={['auto', 'auto']} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#ffffff',
                    borderRadius: '12px',
                    border: '1px solid #e2e8f0',
                    boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)',
                    fontSize: '12px',
                  }}
                  formatter={(val: any) => [`${val} ${history.standardUnit}`, history.canonicalName]}
                />
                <Line
                  type="monotone"
                  dataKey="value"
                  stroke="#059669"
                  strokeWidth={2.5}
                  dot={{ r: 5, fill: '#059669', strokeWidth: 2, stroke: '#ffffff' }}
                  activeDot={{ r: 7, fill: '#047857' }}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        ) : (
          <div className="py-16 text-center">
            <Activity className="w-10 h-10 text-slate-300 mx-auto mb-2" />
            <p className="text-xs text-slate-500">No test observations available yet for this biomarker.</p>
          </div>
        )}
      </div>

      {/* Historical Readings Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
          <h3 className="font-bold text-slate-900 text-sm">Chronological Reading History</h3>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="bg-slate-50 text-slate-500 uppercase tracking-wider font-semibold border-b border-slate-100">
                <th className="py-3 px-6">Date</th>
                <th className="py-3 px-6">Observed Value</th>
                <th className="py-3 px-6">Reference Interval</th>
                <th className="py-3 px-6">Status</th>
                <th className="py-3 px-6 text-right">Source Report</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-slate-700">
              {history.dataPoints.map((dp) => (
                <tr key={dp.measurementId} className="hover:bg-slate-50/80 transition-colors">
                  <td className="py-3.5 px-6 font-medium text-slate-900">
                    {new Date(dp.timestamp).toLocaleDateString(undefined, {
                      year: 'numeric',
                      month: 'short',
                      day: 'numeric',
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </td>
                  <td className="py-3.5 px-6 font-mono font-bold text-slate-900">
                    {dp.observedValue} {dp.unit}
                  </td>
                  <td className="py-3.5 px-6 text-slate-500 font-mono text-[11px]">
                    {dp.referenceIntervalText || 'Standard'}
                  </td>
                  <td className="py-3.5 px-6">
                    <span
                      className={`inline-block px-2.5 py-0.5 rounded-full text-[10px] font-bold ${
                        dp.status === 'NORMAL'
                          ? 'bg-emerald-100 text-emerald-800'
                          : dp.status === 'CRITICAL'
                          ? 'bg-rose-100 text-rose-800'
                          : 'bg-amber-100 text-amber-800'
                      }`}
                    >
                      {dp.status}
                    </span>
                  </td>
                  <td className="py-3.5 px-6 text-right">
                    <Link
                      to={`/reports/${dp.reportId}`}
                      className="text-xs font-semibold text-emerald-700 hover:underline"
                    >
                      View Report &rarr;
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
