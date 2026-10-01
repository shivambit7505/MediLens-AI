import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { biomarkerApi } from '../api/biomarkerApi';
import { Biomarker } from '../types';
import { TrendingUp, Search, Activity, ChevronRight } from 'lucide-react';

export const BiomarkersPage: React.FC = () => {
  const [biomarkers, setBiomarkers] = useState<Biomarker[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');

  useEffect(() => {
    const fetchBiomarkers = async () => {
      try {
        const data = await biomarkerApi.getBiomarkers();
        setBiomarkers(data);
      } catch (err) {
        console.error('Failed to load biomarkers:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchBiomarkers();
  }, []);

  const categories = ['ALL', ...Array.from(new Set(biomarkers.map((b) => b.category || 'General')))];

  const filteredBiomarkers = biomarkers.filter((b) => {
    const matchesSearch = b.canonicalName.toLowerCase().includes(search.toLowerCase()) ||
      (b.codeLoinc && b.codeLoinc.includes(search));
    const matchesCategory = selectedCategory === 'ALL' || (b.category || 'General') === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Canonical Biomarker Catalog</h1>
        <p className="text-xs sm:text-sm text-slate-500 mt-1">
          Standardized clinical tests mapped to LOINC vocabulary and deterministic reference guidelines
        </p>
      </div>

      {/* Filter and Search Bar */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <Search className="w-4 h-4 absolute left-3.5 top-3 text-slate-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search biomarkers by name or LOINC code..."
            className="w-full pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-xl text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-600"
          />
        </div>

        <div className="flex items-center space-x-2 overflow-x-auto pb-1 sm:pb-0">
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              className={`px-3 py-2 rounded-xl text-xs font-semibold whitespace-nowrap transition-colors ${
                selectedCategory === cat
                  ? 'bg-emerald-600 text-white shadow-xs'
                  : 'bg-white border border-slate-200 text-slate-600 hover:bg-slate-50'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>
      </div>

      {/* Biomarker Grid */}
      {loading ? (
        <div className="py-20 flex flex-col items-center justify-center space-y-2">
          <div className="w-8 h-8 border-4 border-emerald-600 border-t-transparent rounded-full animate-spin"></div>
          <p className="text-xs text-slate-400">Loading catalog...</p>
        </div>
      ) : filteredBiomarkers.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredBiomarkers.map((b) => (
            <Link
              key={b.id}
              to={`/biomarkers/${b.id}`}
              className="group bg-white p-5 rounded-2xl border border-slate-200 shadow-xs hover:border-emerald-500/50 hover:shadow-sm transition-all flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between mb-2">
                  <span className="px-2 py-0.5 rounded-md bg-slate-100 text-slate-600 text-[10px] font-semibold uppercase tracking-wider">
                    {b.category || 'General'}
                  </span>
                  {b.codeLoinc && (
                    <span className="font-mono text-[10px] text-slate-400">
                      LOINC: {b.codeLoinc}
                    </span>
                  )}
                </div>

                <h3 className="text-base font-bold text-slate-900 group-hover:text-emerald-700 transition-colors">
                  {b.canonicalName}
                </h3>
                <p className="text-xs text-slate-400 mt-1 font-mono">
                  Standard Unit: <span className="font-semibold text-slate-700">{b.standardUnit}</span>
                </p>
              </div>

              <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-xs text-emerald-700 font-semibold group-hover:translate-x-0.5 transition-transform">
                <span className="flex items-center space-x-1">
                  <TrendingUp className="w-3.5 h-3.5" />
                  <span>View Longitudinal Trend</span>
                </span>
                <ChevronRight className="w-4 h-4" />
              </div>
            </Link>
          ))}
        </div>
      ) : (
        <div className="py-16 text-center bg-white rounded-2xl border border-slate-200">
          <Activity className="w-12 h-12 text-slate-300 mx-auto mb-3" />
          <h4 className="text-sm font-semibold text-slate-700">No biomarkers matched your criteria</h4>
          <p className="text-xs text-slate-400 mt-1">Try refining your search keyword or category filter.</p>
        </div>
      )}
    </div>
  );
};
