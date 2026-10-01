import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { dashboardApi } from '../api/dashboardApi';
import { DashboardSummary } from '../types';
import { 
  FileText, 
  Activity, 
  AlertCircle, 
  AlertTriangle, 
  UploadCloud, 
  TrendingUp, 
  ArrowRight,
  Clock
} from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        const data = await dashboardApi.getSummary();
        setSummary(data);
      } catch (err: any) {
        setError('Failed to load clinical dashboard metrics.');
      } finally {
        setLoading(false);
      }
    };

    fetchDashboard();
  }, []);

  if (loading) {
    return (
      <div className="py-20 flex flex-col items-center justify-center space-y-3">
        <div className="w-10 h-10 border-4 border-emerald-600 border-t-transparent rounded-full animate-spin"></div>
        <p className="text-xs text-slate-500 font-medium">Loading clinical metrics...</p>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {/* Welcome & Overview Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-slate-200">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">
            Welcome back, {user?.firstName}!
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Clinical intelligence overview and longitudinal laboratory trajectory tracking
          </p>
        </div>
        <Link
          to="/upload"
          className="inline-flex items-center space-x-2 px-4 py-2.5 rounded-xl text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 shadow-sm transition-all"
        >
          <UploadCloud className="w-4 h-4" />
          <span>Upload Lab Report</span>
        </Link>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-amber-50 border border-amber-200 text-amber-800 text-xs flex items-center space-x-2">
          <AlertCircle className="w-4 h-4 flex-shrink-0 text-amber-600" />
          <span>{error}</span>
        </div>
      )}

      {/* Metric Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        {/* Total Reports */}
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex items-center justify-between">
          <div>
            <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Total Reports</p>
            <h3 className="text-2xl font-bold text-slate-900 mt-1.5">{summary?.totalReports ?? 0}</h3>
            <p className="text-[11px] text-slate-400 mt-1">Ingested diagnostic files</p>
          </div>
          <div className="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center">
            <FileText className="w-6 h-6" />
          </div>
        </div>

        {/* Total Measurements */}
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex items-center justify-between">
          <div>
            <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Observations</p>
            <h3 className="text-2xl font-bold text-slate-900 mt-1.5">{summary?.totalMeasurements ?? 0}</h3>
            <p className="text-[11px] text-slate-400 mt-1">Validated biomarkers</p>
          </div>
          <div className="w-12 h-12 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
            <Activity className="w-6 h-6" />
          </div>
        </div>

        {/* Abnormal Findings */}
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex items-center justify-between">
          <div>
            <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Abnormal Findings</p>
            <h3 className="text-2xl font-bold text-amber-600 mt-1.5">{summary?.abnormalCount ?? 0}</h3>
            <p className="text-[11px] text-slate-400 mt-1">Out-of-range biomarkers</p>
          </div>
          <div className="w-12 h-12 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center">
            <AlertTriangle className="w-6 h-6" />
          </div>
        </div>

        {/* Critical Alerts */}
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex items-center justify-between relative overflow-hidden">
          {(summary?.criticalCount ?? 0) > 0 && (
            <span className="absolute top-2 right-2 flex h-3 w-3">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-rose-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-3 w-3 bg-rose-500"></span>
            </span>
          )}
          <div>
            <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Critical Alerts</p>
            <h3 className={`text-2xl font-bold mt-1.5 ${(summary?.criticalCount ?? 0) > 0 ? 'text-rose-600' : 'text-slate-900'}`}>
              {summary?.criticalCount ?? 0}
            </h3>
            <p className="text-[11px] text-slate-400 mt-1">Immediate review flags</p>
          </div>
          <div className="w-12 h-12 rounded-xl bg-rose-50 text-rose-600 flex items-center justify-center">
            <AlertCircle className="w-6 h-6" />
          </div>
        </div>
      </div>

      {/* Quick Action Navigation Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
        <Link
          to="/upload"
          className="group p-6 bg-gradient-to-br from-emerald-600 to-teal-700 rounded-2xl text-white shadow-sm hover:shadow-md transition-all flex items-center justify-between"
        >
          <div>
            <span className="inline-block px-2.5 py-1 rounded-md bg-white/20 text-xs font-semibold uppercase tracking-wider mb-2">
              Action Center
            </span>
            <h3 className="text-xl font-bold">Upload New Lab Report</h3>
            <p className="text-xs text-emerald-100 mt-1 max-w-sm">
              Ingest multi-page PDF or image lab reports with automatic OCR extraction and range checking.
            </p>
          </div>
          <div className="w-10 h-10 rounded-xl bg-white/20 flex items-center justify-center group-hover:translate-x-1 transition-transform">
            <ArrowRight className="w-5 h-5" />
          </div>
        </Link>

        <Link
          to="/biomarkers"
          className="group p-6 bg-white border border-slate-200 rounded-2xl shadow-xs hover:border-emerald-300 hover:shadow-sm transition-all flex items-center justify-between"
        >
          <div>
            <span className="inline-block px-2.5 py-1 rounded-md bg-slate-100 text-slate-600 text-xs font-semibold uppercase tracking-wider mb-2">
              Longitudinal Analytics
            </span>
            <h3 className="text-xl font-bold text-slate-900">Explore Biomarker Trajectories</h3>
            <p className="text-xs text-slate-500 mt-1 max-w-sm">
              Visualize historical changes, percentage deltas, and statistical curves over time.
            </p>
          </div>
          <div className="w-10 h-10 rounded-xl bg-slate-100 text-slate-700 flex items-center justify-center group-hover:translate-x-1 group-hover:bg-emerald-50 group-hover:text-emerald-700 transition-all">
            <TrendingUp className="w-5 h-5" />
          </div>
        </Link>
      </div>

      {/* Recent Ingested Reports Section */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <Clock className="w-4 h-4 text-slate-400" />
            <h3 className="font-bold text-slate-900 text-sm">Recent Lab Reports</h3>
          </div>
          <Link to="/reports" className="text-xs font-semibold text-emerald-700 hover:underline">
            View All Reports &rarr;
          </Link>
        </div>

        {summary?.recentReports && summary.recentReports.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="bg-slate-50 text-slate-500 uppercase tracking-wider font-semibold border-b border-slate-100">
                  <th className="py-3 px-6">File Name</th>
                  <th className="py-3 px-6">Status</th>
                  <th className="py-3 px-6">Pages</th>
                  <th className="py-3 px-6">Observations</th>
                  <th className="py-3 px-6">Uploaded</th>
                  <th className="py-3 px-6 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-slate-700">
                {summary.recentReports.map((report) => (
                  <tr key={report.id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="py-3.5 px-6 font-medium text-slate-900 flex items-center space-x-2">
                      <FileText className="w-4 h-4 text-slate-400" />
                      <span className="truncate max-w-[200px]">{report.originalFilename}</span>
                    </td>
                    <td className="py-3.5 px-6">
                      <span
                        className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                          report.status === 'COMPLETED'
                            ? 'bg-emerald-100 text-emerald-800'
                            : report.status === 'FAILED'
                            ? 'bg-rose-100 text-rose-800'
                            : 'bg-amber-100 text-amber-800'
                        }`}
                      >
                        {report.status}
                      </span>
                    </td>
                    <td className="py-3.5 px-6">{report.pageCount}</td>
                    <td className="py-3.5 px-6 font-semibold">{report.measurementCount}</td>
                    <td className="py-3.5 px-6 text-slate-400">
                      {new Date(report.createdAt).toLocaleDateString()}
                    </td>
                    <td className="py-3.5 px-6 text-right">
                      <Link
                        to={`/reports/${report.id}`}
                        className="inline-flex items-center space-x-1 text-xs font-semibold text-emerald-700 hover:text-emerald-800 hover:underline"
                      >
                        <span>View Analysis</span>
                        <ArrowRight className="w-3.5 h-3.5" />
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="py-12 px-4 text-center">
            <FileText className="w-12 h-12 text-slate-300 mx-auto mb-3" />
            <h4 className="text-sm font-semibold text-slate-700">No lab reports ingested yet</h4>
            <p className="text-xs text-slate-400 max-w-sm mx-auto mt-1 mb-4">
              Upload your first diagnostic laboratory report (PDF or PNG) to activate extraction and trends.
            </p>
            <Link
              to="/upload"
              className="inline-flex items-center space-x-1.5 px-4 py-2 rounded-xl text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 shadow-sm"
            >
              <UploadCloud className="w-4 h-4" />
              <span>Upload First Report</span>
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};
