import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { reportApi } from '../api/reportApi';
import { Report } from '../types';
import { FileText, UploadCloud, Search, ChevronRight } from 'lucide-react';

export const ReportListPage: React.FC = () => {
  const [reports, setReports] = useState<Report[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  useEffect(() => {
    const fetchReports = async () => {
      try {
        const data = await reportApi.getReports(0, 50);
        setReports(data.content || []);
      } catch (err) {
        console.error('Failed to load reports:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchReports();
  }, []);

  const filteredReports = reports.filter((r) =>
    r.originalFilename.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Diagnostic Lab Reports</h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Historical archive of all uploaded laboratory documents and analyzed panels
          </p>
        </div>
        <Link
          to="/upload"
          className="inline-flex items-center space-x-2 px-4 py-2 rounded-xl text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 shadow-sm"
        >
          <UploadCloud className="w-4 h-4" />
          <span>Upload Report</span>
        </Link>
      </div>

      {/* Filter / Search Bar */}
      <div className="relative max-w-md">
        <Search className="w-4 h-4 absolute left-3.5 top-3 text-slate-400" />
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search reports by filename..."
          className="w-full pl-10 pr-4 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-600"
        />
      </div>

      {/* Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
        {loading ? (
          <div className="py-20 flex flex-col items-center justify-center space-y-2">
            <div className="w-8 h-8 border-4 border-emerald-600 border-t-transparent rounded-full animate-spin"></div>
            <p className="text-xs text-slate-400">Loading reports...</p>
          </div>
        ) : filteredReports.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="bg-slate-50 text-slate-500 uppercase tracking-wider font-semibold border-b border-slate-100">
                  <th className="py-3.5 px-6">Document Name</th>
                  <th className="py-3.5 px-6">Status</th>
                  <th className="py-3.5 px-6">Pages</th>
                  <th className="py-3.5 px-6">Observations</th>
                  <th className="py-3.5 px-6">Date Ingested</th>
                  <th className="py-3.5 px-6 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-slate-700">
                {filteredReports.map((report) => (
                  <tr key={report.id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="py-4 px-6 font-semibold text-slate-900 flex items-center space-x-3">
                      <div className="w-8 h-8 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center flex-shrink-0">
                        <FileText className="w-4 h-4" />
                      </div>
                      <span className="truncate max-w-[280px]">{report.originalFilename}</span>
                    </td>
                    <td className="py-4 px-6">
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
                    <td className="py-4 px-6">{report.pageCount}</td>
                    <td className="py-4 px-6 font-semibold text-slate-900">{report.measurementCount}</td>
                    <td className="py-4 px-6 text-slate-500">
                      {new Date(report.createdAt).toLocaleDateString(undefined, {
                        year: 'numeric',
                        month: 'short',
                        day: 'numeric',
                      })}
                    </td>
                    <td className="py-4 px-6 text-right">
                      <Link
                        to={`/reports/${report.id}`}
                        className="inline-flex items-center space-x-1 px-3 py-1.5 rounded-lg text-xs font-semibold text-emerald-700 bg-emerald-50 hover:bg-emerald-100 transition-colors"
                      >
                        <span>View Details</span>
                        <ChevronRight className="w-3.5 h-3.5" />
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="py-16 text-center">
            <FileText className="w-12 h-12 text-slate-300 mx-auto mb-3" />
            <h4 className="text-sm font-semibold text-slate-700">No reports found</h4>
            <p className="text-xs text-slate-400 mt-1">Upload a lab report to start tracking your health data.</p>
          </div>
        )}
      </div>
    </div>
  );
};
